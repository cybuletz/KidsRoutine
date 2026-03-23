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

      // 4. GENERATE NEW TASKS (if no cache for today or all cached were used)
      const systemPrompt = `You are a children's task generator for ages ${childAge}+.
Generate a fun, engaging, UNIQUE task that is age-appropriate and safe.

${preferences.length > 0 ? `⚠️ CRITICAL - YOU MUST FOLLOW THIS:
The task category MUST be EXACTLY ONE of these: ${preferences.join(" OR ")}
Do NOT generate a task from any other category.
Do NOT interpret this loosely - pick ONE of these EXACTLY.` : ""}

IMPORTANT - CREATE DIVERSE, SPECIFIC TITLES:
- NO generic titles like "Rainbow Art" or repeated patterns
- Make titles ACTION-ORIENTED showing WHAT the child does
- Use emojis to make titles appealing
- Each title should be DIFFERENT and CREATIVE

TASK EXAMPLES BY CATEGORY:
CREATIVE: "🎨 Mix Paint Colors Like a Real Artist", "📖 Write a Funny Story About Your Pet", "🎭 Act Out a Movie Scene", "🎪 Create a Circus Poster"
SPORTS: "⚽ Invent Rules for a Brand New Sport", "🏃 Race Against the Clock for 30 Seconds", "🎾 Bounce a Ball 20 Times", "🤸 Do 10 Jumping Jacks", "🏀 Create a Basketball Course in Your Room"
LEARNING: "📚 Read 5 Pages of Your Favorite Book", "🔬 Mix Potions with Safe Kitchen Items", "🧮 Solve 5 Math Puzzles", "✏️ Write a Short Story About Dragons", "🧪 Do a Simple Science Experiment"
HEALTH: "🥗 Taste a New Healthy Snack", "🧘 Try 5 Minutes of Meditation", "💪 Do 10 Push-ups or Squats", "🚴 Ride Your Bike for 15 Minutes", "🤸 Stretch for 3 Minutes"
MORNING_ROUTINE: "🛏️ Make Your Bed Without Help", "🧼 Brush Teeth for 2 Minutes", "🚿 Take a Quick Shower", "👕 Pick Out Your Outfit"
SOCIAL: "👥 Play a Game with a Friend", "💬 Ask Someone About Their Day", "🤝 Help a Family Member with a Chore", "🎮 Play Video Games with a Friend"
EMOTIONAL: "❤️ Draw What Makes You Happy", "😊 Do 3 Things You Love Today", "🎵 Sing Your Favorite Song", "📝 Write 3 Things You're Grateful For"
REAL_LIFE: "🧹 Sweep the Kitchen Floor", "🍽️ Set the Dinner Table", "🧺 Fold Your Clothes", "🧼 Wash Your Hands"

TASK REQUIREMENTS:
- Title: Short, fun, emoji-enabled, SPECIFIC, ACTION-FOCUSED
- Description: Clear, child-friendly, under 100 chars
- Duration: 5-60 seconds
- Category: ${preferences.length > 0 ? `MUST be EXACTLY one of: ${preferences.join(" or ")}` : "Any appropriate category"}
- Difficulty: EASY, MEDIUM, or HARD
- Type: LOGIC, REAL_LIFE, CREATIVE, LEARNING, EMOTIONAL, CO_OP, or SOCIAL
- XP Reward: 10-50

${recentCompletions && recentCompletions.length > 0 ? `AVOID these recent tasks: ${recentCompletions.join(", ")}` : ""}

Return ONLY valid JSON (no markdown):
{
  "title": "string",
  "description": "string",
  "estimatedDurationSec": number,
  "category": "string",
  "difficulty": "string",
  "xpReward": number,
  "type": "string"
}`;

      const prompt = `Generate ONE task for a ${childAge}-year-old child.
${preferences.length > 0 ? `STRICT REQUIREMENT: Category MUST be ${preferences.join(" or ")} - pick EXACTLY ONE of these, do not deviate.` : ""}
Make it fun, specific, and age-appropriate.
Use an emoji in the title.
Do NOT create generic titles.`;

      // Generate 10 tasks per day (more variety, less repeats)
      const tasksToGenerate = 10;

      for (let i = 0; i < tasksToGenerate; i++) {
        try {
          const response = await callGemini(apiKey, systemPrompt, prompt);

          let jsonStr = response.trim();

          if (!jsonStr.endsWith('}')) {
            const openBraces = (jsonStr.match(/\{/g) || []).length;
            const closeBraces = (jsonStr.match(/\}/g) || []).length;

            if (openBraces > closeBraces) {
              for (let j = 0; j < openBraces - closeBraces; j++) {
                jsonStr += '}';
              }
            }
          }

          const task = JSON.parse(jsonStr);

          if (!validateTaskSchema(task)) {
            console.warn("[AIGeneration] Invalid task schema, skipping");
            continue;
          }

          // ✅ VALIDATE PREFERENCES
          if (preferences.length > 0 && !preferences.includes(task.category)) {
            console.warn(`[AIGeneration] Task category "${task.category}" not in preferences ${preferences}, skipping`);
            continue;
          }

          const isSafe = await validateTaskSafety(task);
          if (!isSafe) {
            console.warn("[AIGeneration] Task failed safety check, skipping");
            continue;
          }

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

          if (tasks.length >= 5) break; // Stop after 5 valid tasks found
        } catch (e) {
          console.error("[AIGeneration] Failed to parse task:", e);
          continue;
        }
      }

      if (tasks.length === 0) {
        throw new functions.https.HttpsError(
          "internal",
          "Failed to generate valid tasks"
        );
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

      // 4. GENERATE NEW CHALLENGES (if no cache for today)
      const systemPrompt = `You are a children's challenge (habit) generator for ages ${childAge}+.
Generate a multi-day challenge that builds healthy habits and is ENGAGING and MOTIVATING.

${goals.length > 0 ? `⚠️ CRITICAL - YOU MUST FOLLOW THIS:
The challenge category MUST be EXACTLY ONE of these: ${goals.join(" OR ")}
Do NOT generate a challenge from any other category.
Do NOT interpret this loosely - pick ONE of these EXACTLY.` : ""}

IMPORTANT - CREATE DIVERSE, SPECIFIC TITLES:
- NO generic or repeated titles
- Make titles SPECIFIC showing the HABIT/GOAL clearly
- Use emojis and motivating language
- Each challenge should have a UNIQUE title

CHALLENGE EXAMPLES BY CATEGORY:
SLEEP: "🌙 Bedtime Champion: In Bed Before 9 PM", "😴 Sleep Hero: 8 Hours Every Night", "🌙 Dream Warrior: Consistent Bedtime"
HEALTH: "💪 Movement Master: 10 Mins Exercise Daily", "🥗 Veggie Explorer: Try New Vegetables", "🏃 Active Kid: Play Outside 30 Mins Daily"
LEARNING: "📚 Page Turner: Read 20+ Pages Daily", "🧠 Brain Booster: Learn One New Thing Daily", "📖 Story Lover: Read Before Bed"
SOCIAL: "🤝 Kindness Quest: One Good Deed Daily", "👥 Friendship Champion: Call a Friend Weekly", "💬 Good Listener: Ask 3 Questions Daily"
SCREEN_TIME: "🎮 Screen Time Boss: 1 Hour Max Daily", "📱 Digital Detox: Phone-Free Meals", "🎬 Smart Watcher: Choose 1 Show Daily"
CREATIVITY: "🎨 Artist's Week: Create Art Daily", "🎵 Music Maker: Learn One Song", "✏️ Story Writer: Write 100 Words Daily"

CHALLENGE REQUIREMENTS:
- Title: Short, motivating (under 50 chars), SPECIFIC
- Description: Clear, achievable (under 100 chars)
- Duration: 3-30 days (age-appropriate)
- Category: ${goals.length > 0 ? `MUST be EXACTLY one of: ${goals.join(" or ")}` : "SLEEP, SCREEN_TIME, HEALTH, SOCIAL, LEARNING, CREATIVITY"}
- Success condition: Clear, measurable per day (e.g., "In bed by 9 PM" not "Sleep well")

Return ONLY valid JSON (no markdown):
{
  "title": "string",
  "description": "string",
  "durationDays": number,
  "category": "string",
  "successCondition": "string"
}`;

      const prompt = `Generate ONE engaging habit-building challenge for a ${childAge}-year-old.
${goals.length > 0 ? `STRICT REQUIREMENT: Category MUST be ${goals.join(" or ")} - pick EXACTLY ONE of these, do not deviate.` : ""}
Make it motivating and achievable.
Create a SPECIFIC title clearly showing what habit to build.
Include an emoji in the title.
Success condition must be specific and measurable (e.g., "Complete 20 push-ups" not "Be strong").`;

      // Generate 10 challenges per day (more variety, less repeats)
      const challengesToGenerate = 10;

      for (let i = 0; i < challengesToGenerate; i++) {
        try {
          const response = await callGemini(apiKey, systemPrompt, prompt);

          let jsonStr = response.trim();

          if (!jsonStr.endsWith('}')) {
            const openBraces = (jsonStr.match(/\{/g) || []).length;
            const closeBraces = (jsonStr.match(/\}/g) || []).length;

            if (openBraces > closeBraces) {
              for (let j = 0; j < openBraces - closeBraces; j++) {
                jsonStr += '}';
              }
            }
          }

          const challenge = JSON.parse(jsonStr);

          if (!validateChallengeSchema(challenge)) {
            console.warn("[AIGeneration] Invalid challenge schema, skipping");
            continue;
          }

          // ✅ VALIDATE GOALS
          if (goals.length > 0 && !goals.includes(challenge.category)) {
            console.warn(`[AIGeneration] Challenge category "${challenge.category}" not in goals ${goals}, skipping`);
            continue;
          }

          const isSafe = await validateChallengeSafety(challenge);
          if (!isSafe) {
            console.warn(
              "[AIGeneration] Challenge failed safety check, skipping"
            );
            continue;
          }

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

          if (challenges.length >= 5) break; // Stop after 5 valid challenges found
        } catch (e) {
          console.error(
            "[AIGeneration] Failed to parse challenge:",
            e
          );
          continue;
        }
      }

      if (challenges.length === 0) {
        throw new functions.https.HttpsError(
          "internal",
          "Failed to generate valid challenges"
        );
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
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`,
      {
        contents: [
          {
            parts: [
              {
                text: `${systemPrompt}\n\n${userPrompt}`,
              },
            ],
          },
        ],
        generationConfig: {
          maxOutputTokens: 1000,
          temperature: 0.9,
          responseMimeType: "application/json",
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
        timeout: 30000,
      }
    );

    const parts = response.data.candidates?.[0]?.content?.parts || [];
    return parts.map((p: any) => p.text || "").join("");
  } catch (err: any) {
    console.error("Gemini error:", err.response?.data || err.message);
    throw new Error("AI generation failed");
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