import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import axios from "axios";

// ===== HELPER FUNCTION TO GET API KEY FROM REMOTE CONFIG =====

async function getGeminiApiKey(): Promise<string> {
  try {
    const remoteConfig = admin.remoteConfig();
    const template = await remoteConfig.getTemplate();
    const apiKey = (template.parameters["gemini_api_key"]?.defaultValue as any)?.value ||
                   template.parameters["gemini_api_key"]?.defaultValue;

    if (!apiKey) {
      throw new Error("gemini_api_key not found in Remote Config");
    }

    return String(apiKey);
  } catch (error) {
    console.error("Failed to fetch API key from Remote Config:", error);
    throw new functions.https.HttpsError(
      "internal",
      "Failed to load configuration"
    );
  }
}

// ===== AI TASK GENERATION =====

export const generateTasksAI = functions.https.onCall(
  async (data: any, context: any) => {
    const db = admin.firestore();

    const userId = context.auth?.uid;
    if (!userId) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated"
      );
    }

    const {
      familyId,
      childAge,
      preferences = [],
      recentCompletions = [],
      tier = "FREE",
      count = 1,
    } = data;

    if (!familyId || !childAge) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "familyId and childAge required"
      );
    }

    try {
      console.log(`[AIGeneration] Generating ${count} task(s) for age ${childAge}`);
      console.log(`[AIGeneration] Preferences: ${preferences.join(", ")}`);

      // 1. CHECK QUOTA
      const quotaDoc = await db.collection("ai_quotas").doc(userId).get();

      const quota = quotaDoc.data() || {
        tier: tier,
        tasksGenerated: 0,
        tasksLimit: tier === "FREE" ? 1 : tier === "PRO" ? 20 : 999,
        challengesGenerated: 0,
        challengesLimit: tier === "FREE" ? 0 : tier === "PRO" ? 5 : 50,
        resetDate: admin.firestore.Timestamp.now(),
      };

      // Reset quota if date changed
      const now = new Date();
      const resetDate = quota.resetDate.toDate();
      if (
        now.getDate() !== resetDate.getDate() ||
        now.getMonth() !== resetDate.getMonth()
      ) {
        quota.tasksGenerated = 0;
        quota.challengesGenerated = 0;
        quota.resetDate = admin.firestore.Timestamp.now();
      }

      // Check if user exceeded quota
      if (quota.tasksGenerated >= quota.tasksLimit) {
        throw new functions.https.HttpsError(
          "resource-exhausted",
          `Task generation limit reached: ${quota.tasksGenerated}/${quota.tasksLimit}`
        );
      }

      // 2. GET GEMINI API KEY FROM REMOTE CONFIG
      const apiKey = await getGeminiApiKey();

      // 3. SMART CACHE STRATEGY
      const today = new Date();
      const dateKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      const preferencesKey = preferences.length > 0 ? preferences.sort().join("_") : "all";
      const cacheKey = `${familyId}_${childAge}_${preferencesKey}_${dateKey}_gemini`;

      // Get ALL cached tasks for today
      const cachedResult = await db
        .collection("ai_generated_tasks")
        .where("cacheKey", "==", cacheKey)
        .where("expiresAt", ">", admin.firestore.Timestamp.now())
        .get();

      let tasks: any[] = [];

      // If we have cached tasks, pick one at random (excluding recent completions)
      if (cachedResult.size > 0) {
        console.log(
          `[AIGeneration] Found ${cachedResult.size} cached task(s) for today`
        );

        // Filter out recently completed tasks
        const availableTasks = cachedResult.docs
          .filter(doc => {
            const taskTitle = doc.data().task.title;
            return !recentCompletions.includes(taskTitle);
          })
          .map(doc => doc.data().task);

        if (availableTasks.length === 0) {
          console.log("[AIGeneration] All cached tasks were recently completed, generating new ones");
          // Fall through to generate new tasks
        } else {
          const randomIndex = Math.floor(Math.random() * availableTasks.length);
          const randomCachedTask = availableTasks[randomIndex];
          tasks.push(randomCachedTask);

          quota.tasksGenerated += 1;
          await db.collection("ai_quotas").doc(userId).set(quota);

          console.log(`[AIGeneration] Returning cached task: ${randomCachedTask.title}`);

          return {
            success: true,
            tasks: tasks,
            cached: true,
            quotaRemaining: quota.tasksLimit - quota.tasksGenerated,
          };
        }
      }

      // 4. DEFINE AGE-ADAPTIVE TONE AND EXAMPLES
      let ageVibe = "";
      let difficultyRange = "";

      if (childAge < 7) {
        ageVibe = "Imaginary, playful, silly, and fun. Use simple one-word action verbs.";
        difficultyRange = "5-15 seconds, very simple movements";
      } else if (childAge >= 7 && childAge < 13) {
        ageVibe = "Skill-based, gamified, fun, and slightly competitive. Encourage mastery and progress.";
        difficultyRange = "15-45 seconds, moderate physical effort";
      } else {
        ageVibe = "Fitness-focused, practical, life-skill oriented, and respectful of teen maturity. Avoid 'baby' language.";
        difficultyRange = "30-60 seconds, challenging physical or mental effort";
      }

      // 5. GENERATE NEW TASKS (if no cache for today or all cached were used)
      const minXP = childAge < 7 ? 10 : childAge < 13 ? 15 : 20;
      const maxXP = childAge < 7 ? 25 : childAge < 13 ? 35 : 50;

      const systemPrompt = `### ROLE: Task generator for age ${childAge}.
### TONE: ${ageVibe} (${difficultyRange})

### CONSTRAINTS:
- NO stationary: Drawing, coloring, painting, reading, watching, writing.
- MUST be physical, action-focused, or a quick mental challenge.
${preferences.length > 0 ? `- CATEGORY: Pick EXACTLY ONE from [${preferences.join(", ")}]` : ""}

### JSON SCHEMA (Strict):
{
  "title": "Emoji + Short Action Title",
  "description": "Max 60 chars. Clear, child-friendly instruction.",
  "estimatedDurationSec": 5-60 (Integer),
  "category": "MORNING_ROUTINE|HEALTH|LEARNING|CREATIVITY|SOCIAL|FAMILY|CHORES|OUTDOOR|SLEEP|SCREEN_TIME",
  "difficulty": "EASY|MEDIUM|HARD",
  "xpReward": ${minXP}-${maxXP} (Integer),
  "type": "LOGIC|REAL_LIFE|CREATIVE|LEARNING|EMOTIONAL|CO_OP|SOCIAL"
}

### EXAMPLE (Age ${childAge}):
{"title": "🚀 Moon Jump", "description": "Jump as high as you can 10 times!", "estimatedDurationSec": 20, "category": "HEALTH", "difficulty": "EASY", "xpReward": 20, "type": "REAL_LIFE"}

### RECENT (AVOID): ${recentCompletions.slice(-5).join(", ")}`;

      const themes = ["Space", "Jungle", "Dinosaurs", "Superheroes", "Robots", "Underwater", "Pirates", "Mountains", "Dragons", "Deep Sea"];
      const tasksToGenerate = Math.min(count + 2, 5);

      for (let i = 0; i < tasksToGenerate; i++) {
        const randomTheme = themes[Math.floor(Math.random() * themes.length)];

        const localUserPrompt = `Generate ONE ${randomTheme}-themed task.
Mode: ${childAge < 7 ? "Playful" : childAge < 13 ? "Skill-based" : "Fitness/Practical"}.
Return ONLY the JSON object.`;

        try {
          const response = await callGemini(apiKey, systemPrompt, localUserPrompt);

          let jsonStr = response.trim();

          // More robust regex to strip backticks and markdown code blocks
          jsonStr = jsonStr.replace(/^```json\s*|```\s*$/gm, '');
          jsonStr = jsonStr.replace(/^```\s*|```\s*$/gm, '');

          // Extract the JSON object if there's extra text outside braces
          const firstBrace = jsonStr.indexOf('{');
          const lastBrace = jsonStr.lastIndexOf('}');

          if (firstBrace !== -1 && lastBrace !== -1) {
            jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
          }

          console.log("[AIGeneration] After cleanup:", jsonStr.substring(0, 200));

          // 🛡�� TRUNCATION GUARD - Check if JSON is complete
          if (!jsonStr.trim().endsWith('}')) {
            console.warn("[AIGeneration] Skipping attempt: JSON is incomplete/truncated.");
            continue;
          }

          let task;
          try {
            task = JSON.parse(jsonStr);
          } catch (parseError) {
            console.error("[AIGeneration] JSON.parse failed:", (parseError as Error).message);
            continue;
          }

          console.log("[AIGeneration] Parsed task:", JSON.stringify(task));

          if (!validateTaskSchema(task)) {
            console.warn("[AIGeneration] Invalid task schema:", JSON.stringify(task));
            continue;
          }

          // ✅ VALIDATE PREFERENCES
          const validCategories = ["MORNING_ROUTINE", "HEALTH", "LEARNING", "CREATIVITY", "SOCIAL", "FAMILY", "CHORES", "OUTDOOR", "SLEEP", "SCREEN_TIME"];
          const validTypes = ["LOGIC", "REAL_LIFE", "CREATIVE", "LEARNING", "EMOTIONAL", "CO_OP", "SOCIAL"];
          const validDifficulties = ["EASY", "MEDIUM", "HARD"];

          // Check if category is valid
          if (!validCategories.includes(task.category)) {
            console.warn(`[AIGeneration] Task category "${task.category}" is not valid.`);
            continue;
          }

          // Check if type is valid
          if (!validTypes.includes(task.type)) {
            console.warn(`[AIGeneration] Task type "${task.type}" is not valid.`);
            continue;
          }

          // Check if difficulty is valid
          if (!validDifficulties.includes(task.difficulty)) {
            console.warn(`[AIGeneration] Task difficulty "${task.difficulty}" is not valid.`);
            continue;
          }

          // Check if category matches preferences (if preferences are set)
          if (preferences.length > 0 && !preferences.includes(task.category)) {
            console.warn(`[AIGeneration] Task category "${task.category}" not in preferences.`);
            continue;
          }

          // Check safety
          const isSafe = await validateTaskSafety(task);
          if (!isSafe) {
            console.warn("[AIGeneration] Task failed safety check:", task.title);
            continue;
          }

          console.log("[AIGeneration] ✅ Valid task:", task.title);
          tasks.push(task);

          // Store in cache for today
          await db.collection("ai_generated_tasks").add({
            task: task,
            provider: "gemini",
            cacheKey: cacheKey,
            familyId: familyId,
            childAge: childAge,
            preferences: preferences,
            createdAt: admin.firestore.Timestamp.now(),
            expiresAt: admin.firestore.Timestamp.fromDate(
              new Date(Date.now() + 24 * 60 * 60 * 1000)
            ),
            usageCount: 0,
          });

          if (tasks.length >= count + 1) break;
        } catch (e) {
          console.error("[AIGeneration] Parse error (attempt ${i + 1}):", (e as Error).message);
          continue;
        }
      }

      // Safety fallback
      if (tasks.length === 0) {
        console.warn("[AIGeneration] No tasks generated, returning safety fallback.");
        const fallback = {
          title: "⚡ Quick Energy Blast",
          description: "Do 10 high-knees as fast as a lightning bolt!",
          estimatedDurationSec: 20,
          category: "HEALTH",
          difficulty: "EASY",
          xpReward: 15,
          type: "REAL_LIFE"
        };
        tasks.push(fallback);
      }

      // Return random task from generated set
      const randomTask = tasks[Math.floor(Math.random() * tasks.length)];

      quota.tasksGenerated += 1;
      await db.collection("ai_quotas").doc(userId).set(quota);

      await db.collection("ai_usage").add({
        userId: userId,
        familyId: familyId,
        type: "TASK_GENERATION",
        provider: "gemini",
        taskCount: 1,
        preferences: preferences,
        childAge: childAge,
        timestamp: admin.firestore.Timestamp.now(),
      });

      console.log(
        `[AIGeneration] Generated and cached ${tasks.length} tasks. Returning: ${randomTask.title}. Quota: ${quota.tasksGenerated}/${quota.tasksLimit}`
      );

      return {
        success: true,
        tasks: [randomTask],
        cached: false,
        quotaRemaining: quota.tasksLimit - quota.tasksGenerated,
      };
    } catch (error: any) {
      console.error("[AIGeneration] Error:", error.message);
      throw new functions.https.HttpsError(
        error.code || "internal",
        error.message || "Failed to generate tasks"
      );
    }
  }
);

// ===== AI CHALLENGE GENERATION =====

export const generateChallengesAI = functions.https.onCall(
  async (data: any, context: any) => {
    const db = admin.firestore();

    const userId = context.auth?.uid;
    if (!userId) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated"
      );
    }

    const {
      familyId,
      childAge,
      goals = [],
      tier = "FREE",
      count = 1,
    } = data;

    if (!familyId || !childAge) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "familyId and childAge required"
      );
    }

    try {
      console.log(
        `[AIGeneration] Generating ${count} challenge(s) for age ${childAge}`
      );
      console.log(`[AIGeneration] Goals: ${goals.join(", ")}`);

      // 1. CHECK QUOTA
      const quotaDoc = await db
        .collection("ai_quotas")
        .doc(userId)
        .get();

      const quota = quotaDoc.data() || {
        tier: tier,
        tasksGenerated: 0,
        tasksLimit: tier === "FREE" ? 1 : tier === "PRO" ? 20 : 999,
        challengesGenerated: 0,
        challengesLimit: tier === "FREE" ? 0 : tier === "PRO" ? 5 : 50,
        resetDate: admin.firestore.Timestamp.now(),
      };

      // Reset quota if date changed
      const now = new Date();
      const resetDate = quota.resetDate.toDate();
      if (
        now.getDate() !== resetDate.getDate() ||
        now.getMonth() !== resetDate.getMonth()
      ) {
        quota.tasksGenerated = 0;
        quota.challengesGenerated = 0;
        quota.resetDate = admin.firestore.Timestamp.now();
      }

      // Check if user exceeded quota
      if (tier === "FREE") {
        throw new functions.https.HttpsError(
          "permission-denied",
          "Challenge generation not available on FREE tier"
        );
      }

      if (quota.challengesGenerated >= quota.challengesLimit) {
        throw new functions.https.HttpsError(
          "resource-exhausted",
          `Challenge generation limit reached: ${quota.challengesGenerated}/${quota.challengesLimit}`
        );
      }

      // 2. GET GEMINI API KEY FROM REMOTE CONFIG
      const apiKey = await getGeminiApiKey();

      // 3. SMART CACHE STRATEGY
      const today = new Date();
      const dateKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      const goalsKey = goals.length > 0 ? goals.sort().join("_") : "all";
      const cacheKey = `challenge_${familyId}_${childAge}_${goalsKey}_${dateKey}_gemini`;

      // Get ALL cached challenges for today
      const cachedResult = await db
        .collection("ai_generated_challenges")
        .where("cacheKey", "==", cacheKey)
        .where("expiresAt", ">", admin.firestore.Timestamp.now())
        .get();

      let challenges: any[] = [];

      // If we have cached challenges, pick one at random
      if (cachedResult.size > 0) {
        console.log(
          `[AIGeneration] Found ${cachedResult.size} cached challenge(s) for today`
        );
        const randomIndex = Math.floor(Math.random() * cachedResult.size);
        const randomCachedChallenge = cachedResult.docs[randomIndex].data().challenge;
        challenges.push(randomCachedChallenge);

        quota.challengesGenerated += 1;
        await db.collection("ai_quotas").doc(userId).set(quota);

        console.log(`[AIGeneration] Returning cached challenge: ${randomCachedChallenge.title}`);

        return {
          success: true,
          challenges: challenges,
          cached: true,
          quotaRemaining: quota.challengesLimit - quota.challengesGenerated,
        };
      }

      // 4. DEFINE AGE-ADAPTIVE TONE AND EXAMPLES FOR CHALLENGES
      let ageVibe = "";
      let durationRange = "";

      if (childAge < 7) {
        ageVibe = "Playful, imaginative, short-term habits. Make it fun and achievable for little ones.";
        durationRange = "3-7 days, very simple daily actions";
      } else if (childAge >= 7 && childAge < 13) {
        ageVibe = "Gamified, achievable, builds confidence. Make it feel like a quest or challenge.";
        durationRange = "7-14 days, moderate daily effort";
      } else {
        ageVibe = "Mature, fitness-focused, habit-building. Make it feel empowering and results-oriented.";
        durationRange = "14-30 days, sustained commitment";
      }

      // 5. GENERATE NEW CHALLENGES (if no cache for today)
      const themes = ["Space", "Jungle", "Dinosaurs", "Superheroes", "Robots", "Underwater", "Pirates", "Mountains", "Dragons"];
      const challengesToGenerate = Math.min(count + 2, 5);

      const systemPrompt = `### ROLE: Habit-builder for age ${childAge}+.
### TONE: ${ageVibe} (${durationRange})

### CONSTRAINTS:
- NO stationary: Drawing, reading, watching, or passive screen-time.
- MUST be a daily, measurable habit (not a one-time task).
${goals.length > 0 ? `- GOAL: Pick EXACTLY ONE from [${goals.join(", ")}]` : ""}

### JSON SCHEMA (Strict):
{
  "title": "Emoji + Theme Title",
  "description": "Max 80 chars. Motivate the habit.",
  "durationDays": ${childAge < 7 ? "3-7" : childAge < 13 ? "7-14" : "14-30"} (Integer),
  "category": "HEALTH|SLEEP|SOCIAL|CREATIVITY|LEARNING|SCREEN_TIME",
  "successCondition": "Specific daily action (e.g. 'Do 10 push-ups' NOT 'exercise')"
}

### EXAMPLE (Age ${childAge}):
{"title": "🚀 Galaxy Sleeper", "description": "Power up like a star with 8 hours of sleep!", "durationDays": 7, "category": "SLEEP", "successCondition": "Be in bed with lights out by 9:00 PM."}`;

      for (let i = 0; i < challengesToGenerate; i++) {
        const randomTheme = themes[Math.floor(Math.random() * themes.length)];

        const localUserPrompt = `Generate ONE ${randomTheme}-themed challenge for a ${childAge}-year-old.
Theme: ${randomTheme}.
Vibe: ${childAge < 7 ? "Playful" : childAge < 13 ? "Quest-like" : "Achievement-oriented"}.
Return ONLY the JSON object.`;

        try {
          const response = await callGemini(apiKey, systemPrompt, localUserPrompt);

          let jsonStr = response.trim();

          // More robust regex to strip backticks and markdown code blocks
          jsonStr = jsonStr.replace(/^```json\s*|```\s*$/gm, '');
          jsonStr = jsonStr.replace(/^```\s*|```\s*$/gm, '');

          // Extract the JSON object if there's extra text outside braces
          const firstBrace = jsonStr.indexOf('{');
          const lastBrace = jsonStr.lastIndexOf('}');

          if (firstBrace !== -1 && lastBrace !== -1) {
            jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
          }

          console.log("[AIGeneration] After cleanup:", jsonStr.substring(0, 200));

          // 🛡️ TRUNCATION GUARD
          if (!jsonStr.trim().endsWith('}')) {
            console.warn("[AIGeneration] Skipping attempt: JSON is incomplete/truncated.");
            continue;
          }

          let challenge;
          try {
            challenge = JSON.parse(jsonStr);
          } catch (parseError) {
            console.error("[AIGeneration] JSON.parse failed:", (parseError as Error).message);
            continue;
          }

          console.log("[AIGeneration] Parsed challenge:", JSON.stringify(challenge));

          if (!validateChallengeSchema(challenge)) {
            console.warn("[AIGeneration] Invalid challenge schema:", JSON.stringify(challenge));
            continue;
          }

          // ✅ VALIDATE GOALS
          const validCategories = ["HEALTH", "SLEEP", "SOCIAL", "CREATIVITY", "LEARNING", "SCREEN_TIME"];

          // Check if category is valid
          if (!validCategories.includes(challenge.category)) {
            console.warn(`[AIGeneration] Challenge category "${challenge.category}" is not valid.`);
            continue;
          }

          // Check if category matches goals (if goals are set)
          if (goals.length > 0 && !goals.includes(challenge.category)) {
            console.warn(`[AIGeneration] Challenge category "${challenge.category}" not in goals.`);
            continue;
          }

          // Check if duration is valid
          if (typeof challenge.durationDays !== "number" || challenge.durationDays < 3 || challenge.durationDays > 30) {
            console.warn(`[AIGeneration] Challenge duration ${challenge.durationDays} is not valid.`);
            continue;
          }

          const isSafe = await validateChallengeSafety(challenge);
          if (!isSafe) {
            console.warn("[AIGeneration] Challenge failed safety check:", challenge.title);
            continue;
          }

          console.log("[AIGeneration] ✅ Valid challenge:", challenge.title);
          challenges.push(challenge);

          // Store in cache for today
          await db.collection("ai_generated_challenges").add({
            challenge: challenge,
            provider: "gemini",
            cacheKey: cacheKey,
            familyId: familyId,
            childAge: childAge,
            goals: goals,
            createdAt: admin.firestore.Timestamp.now(),
            expiresAt: admin.firestore.Timestamp.fromDate(
              new Date(Date.now() + 24 * 60 * 60 * 1000)
            ),
            usageCount: 0,
          });

          if (challenges.length >= count + 1) break;
        } catch (e) {
          console.error("[AIGeneration] Parse error (attempt ${i + 1}):", (e as Error).message);
          continue;
        }
      }

      // Safety fallback
      if (challenges.length === 0) {
        console.warn("[AIGeneration] Challenge loop failed, using fallback.");
        const fallback = {
          title: "🌟 7-Day Star Power",
          description: "Build your energy by trying one new fruit or veggie every day!",
          durationDays: 7,
          category: "HEALTH",
          successCondition: "Eat at least one serving of a fruit or vegetable you haven't had today."
        };
        challenges.push(fallback);
      }

      // Return random challenge from generated set
      const randomChallenge = challenges[Math.floor(Math.random() * challenges.length)];

      quota.challengesGenerated += 1;
      await db.collection("ai_quotas").doc(userId).set(quota);

      await db.collection("ai_usage").add({
        userId: userId,
        familyId: familyId,
        type: "CHALLENGE_GENERATION",
        provider: "gemini",
        count: 1,
        goals: goals,
        childAge: childAge,
        timestamp: admin.firestore.Timestamp.now(),
      });

      console.log(
        `[AIGeneration] Generated and cached ${challenges.length} challenges. Returning: ${randomChallenge.title}. Quota: ${quota.challengesGenerated}/${quota.challengesLimit}`
      );

      return {
        success: true,
        challenges: [randomChallenge],
        cached: false,
        quotaRemaining: quota.challengesLimit - quota.challengesGenerated,
      };
    } catch (error: any) {
      console.error("[AIGeneration] Error:", error.message);
      throw new functions.https.HttpsError(
        error.code || "internal",
        error.message || "Failed to generate challenges"
      );
    }
  }
);

// ===== HELPER FUNCTIONS =====

async function callGemini(
  apiKey: string,
  systemPrompt: string,
  userPrompt: string
): Promise<string> {
  try {
    const response = await axios.post(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey}`,
      {
        system_instruction: {
          parts: [{ text: systemPrompt }]
        },
        contents: [
          {
            role: "user",
            parts: [{ text: userPrompt }]
          }
        ],
        generationConfig: {
          maxOutputTokens: 500,
          temperature: 0.7,
          topP: 1.0,
          response_mime_type: "application/json",
        },
        safetySettings: [
          {
            category: "HARM_CATEGORY_DANGEROUS_CONTENT",
            threshold: "BLOCK_MEDIUM_AND_ABOVE",
          },
          {
            category: "HARM_CATEGORY_HATE_SPEECH",
            threshold: "BLOCK_LOW_AND_ABOVE",
          },
        ],
      },
      {
        headers: { "Content-Type": "application/json" },
        timeout: 20000,
      }
    );

    const text = response.data.candidates?.[0]?.content?.parts?.[0]?.text || "";

    console.log(`[DEBUG] Received ${text.length} chars: ${text}`);

    return text;
  } catch (err: any) {
    console.error("Gemini API Error:", err.response?.data || err.message);
    throw new Error("AI failed to return content");
  }
}

function validateTaskSchema(task: any): boolean {
  return (
    task.title &&
    task.description &&
    typeof task.estimatedDurationSec === "number" &&
    task.category &&
    task.difficulty &&
    typeof task.xpReward === "number" &&
    task.type
  );
}

function validateChallengeSchema(challenge: any): boolean {
  return (
    challenge.title &&
    challenge.description &&
    typeof challenge.durationDays === "number" &&
    challenge.category &&
    challenge.successCondition
  );
}

async function validateTaskSafety(task: any): Promise<boolean> {
  const dangerousKeywords = [
    "violence",
    "harm",
    "inappropriate",
    "dangerous",
    "illegal",
  ];
  const text = `${task.title} ${task.description}`.toLowerCase();
  return !dangerousKeywords.some((keyword) => text.includes(keyword));
}

async function validateChallengeSafety(challenge: any): Promise<boolean> {
  const dangerousKeywords = [
    "violence",
    "harm",
    "inappropriate",
    "dangerous",
    "illegal",
  ];
  const text = `${challenge.title} ${challenge.description} ${challenge.successCondition}`.toLowerCase();
  return !dangerousKeywords.some((keyword) => text.includes(keyword));
}