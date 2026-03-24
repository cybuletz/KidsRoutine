import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import axios from "axios";

// ===== CONFIGURATION CONSTANTS =====

const AGE_CONFIGS: any = {
  PRE_SCHOOL: {
    range: [5, 7],
    vibe: "Playful, silly, and imaginative. Use 'Magic' or 'Animal' framing. Simple actions.",
    focus: "Early Movement & Imagination",
    themes: ["Dinosaurs", "Animals", "Fairy Tales", "Outer Space", "Under the Sea"],
    dur: [10, 20],
    xp: [10, 20],
    challengeDays: [2, 5],
  },
  CHILD: {
    range: [8, 10],
    vibe: "Adventurous and gamified. Use 'Hero' or 'Quest' framing. Focus on high energy.",
    focus: "Active Play & Gross Motor Skills",
    themes: ["Jungle", "Space Explorer", "Superheroes", "Underwater Quest", "Island Survival"],
    dur: [20, 40],
    xp: [20, 35],
    challengeDays: [5, 10],
  },
  PRE_TEEN: {
    range: [11, 13],
    vibe: "Competitive and achievement-based. Use 'Level Up' or 'Challenge' framing. Focus on mastery.",
    focus: "Skill Acquisition & Social Confidence",
    themes: ["Robots", "Spies", "Olympics", "Parkour", "Cyberpunk"],
    dur: [30, 50],
    xp: [35, 50],
    challengeDays: [7, 14],
  },
  TEEN: {
    range: [14, 16],
    vibe: "Athletic and scientific. Use 'Training', 'Power', or 'Efficiency' framing. AVOID all childish metaphors.",
    focus: "Physical Conditioning & Cognitive Logic",
    themes: ["Tactical Ops", "Elite Athlete", "Future Tech", "Military Prep", "Bio-Hacking"],
    dur: [45, 60],
    xp: [50, 75],
    challengeDays: [10, 21],
  },
  ADULT_PREP: {
    range: [17, 18],
    vibe: "Professional, minimalist, and elite. Focus on 'Peak Performance' and 'Discipline'.",
    focus: "Young Adult Discipline & Life Optimization",
    themes: ["Minimalist Performance", "Navy SEAL Training", "Startup Founder Focus", "Pro-Athlete Protocol", "Zen Mastery"],
    dur: [60, 90],
    xp: [75, 100],
    challengeDays: [14, 30],
  },
};

const VALID_CATEGORIES = ["MORNING_ROUTINE", "HEALTH", "LEARNING", "CREATIVITY", "SOCIAL", "FAMILY", "CHORES", "OUTDOOR", "SLEEP", "SCREEN_TIME"];
const VALID_TYPES = ["LOGIC", "REAL_LIFE", "CREATIVE", "LEARNING", "EMOTIONAL", "CO_OP", "SOCIAL"];
const VALID_DIFFICULTIES = ["EASY", "MEDIUM", "HARD"];
const VALID_CHALLENGE_CATEGORIES = ["HEALTH", "SLEEP", "SOCIAL", "CREATIVITY", "LEARNING", "SCREEN_TIME"];

// ===== HELPER FUNCTIONS =====

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
            role: "user",
            parts: [
              { text: `Instructions: ${systemPrompt}` },
              { text: `Generate task for: ${userPrompt}` }
            ],
          },
        ],
        generationConfig: {
          maxOutputTokens: 2000,
          temperature: 0.7,
          topP: 1.0
        },
      },
      {
        headers: { "Content-Type": "application/json" },
        timeout: 30000,
      }
    );

    const text = response.data.candidates?.[0]?.content?.parts?.[0]?.text || "";
    return text;
  } catch (err: any) {
    console.error("Gemini API Error:", JSON.stringify(err.response?.data));
    throw new Error("AI provider failed");
  }
}

function getAgeConfig(childAge: number): any {
  return (
    Object.values(AGE_CONFIGS).find((c: any) => childAge >= c.range[0] && childAge <= c.range[1]) as any
  ) || AGE_CONFIGS.CHILD;
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

function validateSafety(obj: any): boolean {
  const dangerousKeywords = [
    "stove",
    "knife",
    "blade",
    "fire",
    "street",
    "road",
    "climb",
    "weapon",
    "harm",
    "violence",
    "illegal",
  ];
  const content = JSON.stringify(obj).toLowerCase();
  return !dangerousKeywords.some((word) => content.includes(word));
}

// ===== AI TASK GENERATION =====

export const generateTasksAI = functions.https.onCall(async (data: any, context: any) => {
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

    // 3. SMART CACHE STRATEGY WITH PREFERENCE FILTERING
    const today = new Date();
    const dateKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
    const preferencesKey = preferences.length > 0 ? preferences.sort().join("_") : "all";
    const cacheKey = `${familyId}_${childAge}_${preferencesKey}_${dateKey}_gemini`;

    console.log(`[AIGeneration] Cache key: ${cacheKey}`);
    console.log(`[AIGeneration] Preferences key: ${preferencesKey}`);

    // Get ALL cached tasks for today with this specific cache key
    const cachedResult = await db
      .collection("ai_generated_tasks")
      .where("cacheKey", "==", cacheKey)
      .where("expiresAt", ">", admin.firestore.Timestamp.now())
      .get();

    let tasks: any[] = [];

    // If we have cached tasks matching this cache key, use them
    if (cachedResult.size > 0) {
      console.log(
        `[AIGeneration] Found ${cachedResult.size} cached task(s) for cache key: ${cacheKey}`
      );

      // GEMINI FIX: Filter cached tasks strictly
      const availableTasks = cachedResult.docs
        .map(doc => ({ ...doc.data().task, docId: doc.id }))
        .filter((task) => {
          const taskTitle = task.title;
          const taskCategory = task.category;

          // 1. Ensure it wasn't recently completed
          const isNotRecent = !recentCompletions.includes(taskTitle);
          if (!isNotRecent) {
            console.log(`[AIGeneration] Skipping cached task (recently completed): ${taskTitle}`);
          }

          // 2. STRICT CHECK: Ensure task matches current preferences
          // Since cache key already includes preferences, this double-checks
          const matchesPrefs = preferences.length === 0 || preferences.includes(taskCategory);
          if (!matchesPrefs) {
            console.log(`[AIGeneration] Skipping cached task (doesn't match preferences): ${taskTitle} (category: ${taskCategory}, requested: [${preferences.join(", ")}])`);
          }

          return isNotRecent && matchesPrefs;
        });

      if (availableTasks.length === 0) {
        console.log("[AIGeneration] All cached tasks filtered out, generating new ones");
      } else {
        const randomIndex = Math.floor(Math.random() * availableTasks.length);
        const randomCachedTask = availableTasks[randomIndex];
        tasks.push(randomCachedTask);

        quota.tasksGenerated += 1;
        await db.collection("ai_quotas").doc(userId).set(quota);

        console.log(`[AIGeneration] ✅ Returning cached task matching preferences: ${randomCachedTask.title}`);

        return {
          success: true,
          tasks: [randomCachedTask],
          cached: true,
          quotaRemaining: quota.tasksLimit - quota.tasksGenerated,
        };
      }
    } else {
      console.log(`[AIGeneration] No cached tasks found for cache key: ${cacheKey}. Will generate new ones.`);
    }

    // 4. GET AGE CONFIG
    const config = getAgeConfig(childAge);
    console.log(`[AIGeneration] Using config: ${Object.keys(AGE_CONFIGS).find((k) => (AGE_CONFIGS[k] as any).range[0] === config.range[0])}`);

    // 5. BUILD SYSTEM PROMPT WITH FULL DETAILS
    const systemPrompt = `### ROLE: Task generator for age ${childAge}.
### TONE: ${config.vibe}
### FOCUS: ${config.focus}

### HARD CONSTRAINTS:
- NO stationary tasks. ACTION-ONLY (physical movement or active mental recall).
- ${childAge >= 14 ? "CRITICAL: Use 'Protocol', 'Set', 'Objective'. NEVER use 'fun/game/play/pretend'." : "Make it engaging and fun."}
- xpReward MUST be integer: ${config.xp[0]}-${config.xp[1]}.
- estimatedDurationSec MUST be integer: ${config.dur[0]}-${config.dur[1]}.
${childAge >= 17 ? "- FOR LEARNING: Task MUST combine physical hold (plank/squat) with mental logic/recall." : ""}
${preferences.length > 0 ? `- CATEGORY REQUIREMENT: Pick EXACTLY ONE from [${preferences.join(", ")}]. This is MANDATORY.` : ""}

### JSON SCHEMA (STRICT):
{
  "title": "string (Emoji + Title, max 40 chars)",
"description": "string (Max 50 chars. STRICT. Cut off at 50.)",
  "estimatedDurationSec": number (integer),
  "category": "MORNING_ROUTINE|HEALTH|LEARNING|CREATIVITY|SOCIAL|FAMILY|CHORES|OUTDOOR|SLEEP|SCREEN_TIME",
  "difficulty": "EASY|MEDIUM|HARD",
  "xpReward": number (integer),
  "type": "LOGIC|REAL_LIFE|CREATIVE|LEARNING|EMOTIONAL|CO_OP|SOCIAL"
}

### EXAMPLES (Age ${childAge}):
${childAge >= 17
  ? `{"title": "⚡ Neural Endurance", "description": "Wall-sit for 90s while listing 5 long-term goals.", "estimatedDurationSec": 90, "category": "HEALTH", "difficulty": "HARD", "xpReward": ${config.xp[1]}, "type": "REAL_LIFE"}`
  : childAge >= 14
    ? `{"title": "💥 Explosive Protocol", "description": "Perform 15 air-squats as fast as possible.", "estimatedDurationSec": 45, "category": "HEALTH", "difficulty": "MEDIUM", "xpReward": ${Math.floor((config.xp[0] + config.xp[1]) / 2)}, "type": "REAL_LIFE"}`
    : childAge >= 11
    ? `{"title": "🥷 Stealth Override", "description": "Balance on one leg for 45s while reciting alphabet backwards.", "estimatedDurationSec": 45, "category": "HEALTH", "difficulty": "MEDIUM", "xpReward": ${Math.floor((config.xp[0] + config.xp[1]) / 2)}, "type": "REAL_LIFE"}`
    : `{"title": "🚀 Rocket Launch", "description": "Countdown from 10 while doing fast-feet until blast off!", "estimatedDurationSec": 30, "category": "HEALTH", "difficulty": "EASY", "xpReward": ${config.xp[0]}, "type": "REAL_LIFE"}`
}

Return ONLY valid JSON. No markdown. No extra text.`;

    console.log(`[AIGeneration] System prompt constructed for age ${childAge}`);

    // 6. GENERATE TASKS
    const tasksToGenerate = Math.min(count + 2, 5);
    console.log(`[AIGeneration] Attempting to generate ${tasksToGenerate} tasks`);

    for (let i = 0; i < tasksToGenerate; i++) {
      const randomTheme = config.themes[Math.floor(Math.random() * config.themes.length)];

      console.log(`[AIGeneration] Attempt ${i + 1}/${tasksToGenerate}: Theme = ${randomTheme}`);

      const userPrompt = `Generate ONE ${randomTheme}-themed task for age ${childAge}.
Focus Mode: ${config.focus}.
Duration: ${config.dur[0]}-${config.dur[1]} seconds.
${preferences.length > 0 ? `REQUIRED CATEGORY: ${preferences.join(" or ")}. The task MUST be in this category.` : ""}
Return ONLY valid JSON object. No markdown, no text.`;

      try {
        const raw = await callGemini(apiKey, systemPrompt, userPrompt);
        console.log(`[AIGeneration] Raw response (${raw.length} chars): ${raw.substring(0, 150)}...`);

        let task;
        try {
          task = JSON.parse(raw);
        } catch (e) {
          console.log("[AIGeneration] Initial parse failed, attempting cleanup...");
          const cleaner = raw.replace(/```json|```/g, "").trim();
          task = JSON.parse(cleaner);
        }

        console.log("[AIGeneration] Parsed task:", JSON.stringify(task));

        if (!validateTaskSchema(task)) {
          console.warn("[AIGeneration] Invalid task schema:", JSON.stringify(task));
          continue;
        }

        // Validate category
        if (!VALID_CATEGORIES.includes(task.category)) {
          console.warn(`[AIGeneration] Task category "${task.category}" is not valid.`);
          continue;
        }

        // Validate type
        if (!VALID_TYPES.includes(task.type)) {
          console.warn(`[AIGeneration] Task type "${task.type}" is not valid.`);
          continue;
        }

        // Validate difficulty
        if (!VALID_DIFFICULTIES.includes(task.difficulty)) {
          console.warn(`[AIGeneration] Task difficulty "${task.difficulty}" is not valid.`);
          continue;
        }

        // Validate duration bounds
        if (task.estimatedDurationSec < config.dur[0] || task.estimatedDurationSec > config.dur[1]) {
          console.warn(`[AIGeneration] Task duration ${task.estimatedDurationSec} out of bounds [${config.dur[0]}-${config.dur[1]}].`);
          continue;
        }

        // Validate XP bounds
        if (task.xpReward < config.xp[0] || task.xpReward > config.xp[1]) {
          console.warn(`[AIGeneration] Task XP ${task.xpReward} out of bounds [${config.xp[0]}-${config.xp[1]}].`);
          continue;
        }

        // Check preferences - CRITICAL
        if (preferences.length > 0 && !preferences.includes(task.category)) {
          console.warn(`[AIGeneration] Task category "${task.category}" not in preferences [${preferences.join(", ")}].`);
          continue;
        }

        // Check safety
        if (!validateSafety(task)) {
          console.warn("[AIGeneration] Task failed safety check:", task.title);
          continue;
        }

        console.log("[AIGeneration] ✅ Valid task passed all validation:", task.title);
        tasks.push(task);

        // Store in cache with explicit preferences stored
        await db.collection("ai_generated_tasks").add({
          task: task,
          provider: "gemini",
          cacheKey: cacheKey,
          familyId: familyId,
          childAge: childAge,
          preferences: preferences,  // Store the preferences for debugging
          createdAt: admin.firestore.Timestamp.now(),
          expiresAt: admin.firestore.Timestamp.fromDate(
            new Date(Date.now() + 24 * 60 * 60 * 1000)
          ),
          usageCount: 0,
        });

        console.log(`[AIGeneration] Task cached with cacheKey: ${cacheKey}. Total valid tasks: ${tasks.length}`);

        if (tasks.length >= count + 1) {
          console.log(`[AIGeneration] Reached target of ${count + 1} tasks. Stopping generation loop.`);
          break;
        }
      } catch (e) {
console.error(`[AIGeneration] Parse error (attempt ${i + 1}):`, (e as Error).message);
        continue;
      }
    }

    // Safety fallback
    if (tasks.length === 0) {
      console.warn("[AIGeneration] No tasks generated, returning safety fallback.");
      const mid = Math.floor((config.dur[0] + config.dur[1]) / 2);
      const fallback = {
        title: "⚡ Quick Energy Blast",
        description: "Do 10 high-knees as fast as a lightning bolt!",
        estimatedDurationSec: mid,
        category: preferences.length > 0 ? preferences[0] : "HEALTH",
        difficulty: "EASY",
        xpReward: config.xp[0],
        type: "REAL_LIFE",
      };
      console.log("[AIGeneration] Fallback task:", JSON.stringify(fallback));
      tasks.push(fallback);
    }

    // Return random task
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
      `[AIGeneration] ✅ TASK GENERATION COMPLETE - Generated and cached ${tasks.length} tasks. Returning: ${randomTask.title}. Quota: ${quota.tasksGenerated}/${quota.tasksLimit}`
    );

    return {
      success: true,
      tasks: [randomTask],
      cached: false,
      quotaRemaining: quota.tasksLimit - quota.tasksGenerated,
    };
  } catch (error: any) {
    console.error("[AIGeneration] ❌ TASK GENERATION FAILED - Error:", error.message);
    throw new functions.https.HttpsError(
      error.code || "internal",
      error.message || "Failed to generate tasks"
    );
  }
});

// ===== AI CHALLENGE GENERATION (SAME FIXES) =====

export const generateChallengesAI = functions.https.onCall(async (data: any, context: any) => {
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
    console.log(`[AIGeneration] Generating ${count} challenge(s) for age ${childAge}`);
    console.log(`[AIGeneration] Goals: ${goals.join(", ")}`);

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

    // 2. GET GEMINI API KEY
    const apiKey = await getGeminiApiKey();

    // 3. CACHE STRATEGY WITH GOAL FILTERING
    const today = new Date();
    const dateKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
    const goalsKey = goals.length > 0 ? goals.sort().join("_") : "all";
    const cacheKey = `challenge_${familyId}_${childAge}_${goalsKey}_${dateKey}_gemini`;

    console.log(`[AIGeneration] Cache key: ${cacheKey}`);
    console.log(`[AIGeneration] Goals key: ${goalsKey}`);

    const cachedResult = await db
      .collection("ai_generated_challenges")
      .where("cacheKey", "==", cacheKey)
      .where("expiresAt", ">", admin.firestore.Timestamp.now())
      .get();

    let challenges: any[] = [];

    if (cachedResult.size > 0) {
      console.log(
        `[AIGeneration] Found ${cachedResult.size} cached challenge(s) for cache key: ${cacheKey}`
      );

      // GEMINI FIX: Filter by goals matching
      const availableChallenges = cachedResult.docs
        .map(doc => ({ ...doc.data().challenge, docId: doc.id }))
        .filter((challenge) => {
          const challengeCategory = challenge.category;

          // STRICT CHECK: Ensure challenge matches current goals
          const matchesGoals = goals.length === 0 || goals.includes(challengeCategory);
          if (!matchesGoals) {
            console.log(`[AIGeneration] Skipping cached challenge (doesn't match goals): category: ${challengeCategory}, requested: [${goals.join(", ")}]`);
          }

          return matchesGoals;
        });

      if (availableChallenges.length === 0) {
        console.log("[AIGeneration] No cached challenges match current goals, generating new ones");
      } else {
        const randomIndex = Math.floor(Math.random() * availableChallenges.length);
        const randomCachedChallenge = availableChallenges[randomIndex];
        challenges.push(randomCachedChallenge);

        quota.challengesGenerated += 1;
        await db.collection("ai_quotas").doc(userId).set(quota);

        console.log(
          `[AIGeneration] ✅ Returning cached challenge matching goals: ${randomCachedChallenge.title}`
        );

        return {
          success: true,
          challenges: [randomCachedChallenge],
          cached: true,
          quotaRemaining: quota.challengesLimit - quota.challengesGenerated,
        };
      }
    } else {
      console.log(`[AIGeneration] No cached challenges found for cache key: ${cacheKey}. Will generate new ones.`);
    }

    // 4. GET AGE CONFIG
    const config = getAgeConfig(childAge);
    console.log(`[AIGeneration] Using config: ${Object.keys(AGE_CONFIGS).find((k) => (AGE_CONFIGS[k] as any).range[0] === config.range[0])}`);

    // 5. BUILD SYSTEM PROMPT
    const systemPrompt = `### ROLE: Habit coach for age ${childAge}+.
### TONE: ${config.vibe}
### FOCUS: ${config.focus}

### HARD CONSTRAINTS:
- NO stationary habits (no reading marathons, no passive screens).
- MUST be a daily, measurable habit for ${config.challengeDays[0]}-${config.challengeDays[1]} days.
- ${childAge >= 14 ? "CRITICAL: Use 'Protocol', 'Set', 'Objective'. NEVER use 'fun/game/play'." : "Make it fun and engaging."}
${goals.length > 0 ? `- GOAL REQUIREMENT: Pick EXACTLY ONE from [${goals.join(", ")}]. This is MANDATORY.` : ""}
${childAge >= 17 ? "- FOR LEARNING: Combine physical discipline with mental recall (plank while solving math)." : ""}

### JSON SCHEMA (STRICT):
{
  "title": "string (Emoji + Title, max 50 chars)",
  "description": "string (Max 80 chars)",
  "durationDays": number (integer),
  "category": "HEALTH|SLEEP|SOCIAL|CREATIVITY|LEARNING|SCREEN_TIME",
  "successCondition": "string (specific daily action)"
}

### EXAMPLES (Age ${childAge}):
${childAge >= 17
  ? `{"title": "🧠 Executive Protocol", "description": "Master discipline through 21 days of peak optimization.", "durationDays": 21, "category": "HEALTH", "successCondition": "5-minute meditation + plank while reciting 3 goals."}`
  : childAge >= 14
    ? `{"title": "💪 Athletic Conditioning", "description": "Build strength and endurance with daily training.", "durationDays": 14, "category": "HEALTH", "successCondition": "Complete 20 minutes of exercise."}`
    : `{"title": "🦁 Explorer Quest", "description": "Go on a daily adventure and discover the world!", "durationDays": 7, "category": "HEALTH", "successCondition": "Play or move for at least 20 minutes outside."}`
}

Return ONLY valid JSON. No markdown. No extra text.`;

    console.log(`[AIGeneration] System prompt constructed for age ${childAge}`);

    // 6. GENERATE CHALLENGES
    const challengesToGenerate = Math.min(count + 2, 5);
    console.log(`[AIGeneration] Attempting to generate ${challengesToGenerate} challenges`);

    for (let i = 0; i < challengesToGenerate; i++) {
      const randomTheme = config.themes[Math.floor(Math.random() * config.themes.length)];

      console.log(`[AIGeneration] Attempt ${i + 1}/${challengesToGenerate}: Theme = ${randomTheme}`);

      const userPrompt = `Generate ONE ${randomTheme}-themed challenge for age ${childAge}.
Focus Mode: ${config.focus}.
Duration: ${config.challengeDays[0]}-${config.challengeDays[1]} days.
${goals.length > 0 ? `REQUIRED CATEGORY: ${goals.join(" or ")}. The challenge MUST be in this category.` : ""}
Return ONLY valid JSON object. No markdown, no text.`;

      try {
        const raw = await callGemini(apiKey, systemPrompt, userPrompt);
        console.log(`[AIGeneration] Raw response (${raw.length} chars): ${raw.substring(0, 150)}...`);

        let challenge;
        try {
          challenge = JSON.parse(raw);
        } catch (e) {
          console.log("[AIGeneration] Initial parse failed, attempting cleanup...");
          const cleaner = raw.replace(/```json|```/g, "").trim();
          challenge = JSON.parse(cleaner);
        }

        console.log("[AIGeneration] Parsed challenge:", JSON.stringify(challenge));

        if (!validateChallengeSchema(challenge)) {
          console.warn("[AIGeneration] Invalid challenge schema:", JSON.stringify(challenge));
          continue;
        }

        // Validate category
        if (!VALID_CHALLENGE_CATEGORIES.includes(challenge.category)) {
          console.warn(`[AIGeneration] Challenge category "${challenge.category}" is not valid.`);
          continue;
        }

        // Check goals - CRITICAL
        if (goals.length > 0 && !goals.includes(challenge.category)) {
          console.warn(`[AIGeneration] Challenge category "${challenge.category}" not in goals [${goals.join(", ")}].`);
          continue;
        }

        // Validate duration
        if (
          challenge.durationDays < config.challengeDays[0] ||
          challenge.durationDays > config.challengeDays[1]
        ) {
          console.warn(
            `[AIGeneration] Challenge duration ${challenge.durationDays} out of bounds [${config.challengeDays[0]}-${config.challengeDays[1]}].`
          );
          continue;
        }

        // Check safety
        if (!validateSafety(challenge)) {
          console.warn("[AIGeneration] Challenge failed safety check:", challenge.title);
          continue;
        }

        console.log("[AIGeneration] ✅ Valid challenge passed all validation:", challenge.title);
        challenges.push(challenge);

        // Store in cache with explicit goals stored
        await db.collection("ai_generated_challenges").add({
          challenge: challenge,
          provider: "gemini",
          cacheKey: cacheKey,
          familyId: familyId,
          childAge: childAge,
          goals: goals,  // Store the goals for debugging
          createdAt: admin.firestore.Timestamp.now(),
          expiresAt: admin.firestore.Timestamp.fromDate(
            new Date(Date.now() + 24 * 60 * 60 * 1000)
          ),
          usageCount: 0,
        });

        console.log(`[AIGeneration] Challenge cached with cacheKey: ${cacheKey}. Total valid challenges: ${challenges.length}`);

        if (challenges.length >= count + 1) {
          console.log(`[AIGeneration] Reached target of ${count + 1} challenges. Stopping generation loop.`);
          break;
        }
      } catch (e) {
console.error(`[AIGeneration] Parse error (attempt ${i + 1}):`, (e as Error).message);
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
        category: goals.length > 0 ? goals[0] : "HEALTH",
        successCondition:
          "Eat at least one serving of a fruit or vegetable you haven't had today.",
      };
      console.log("[AIGeneration] Fallback challenge:", JSON.stringify(fallback));
      challenges.push(fallback);
    }

    // Return random challenge
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
      `[AIGeneration] ✅ CHALLENGE GENERATION COMPLETE - Generated and cached ${challenges.length} challenges. Returning: ${randomChallenge.title}. Quota: ${quota.challengesGenerated}/${quota.challengesLimit}`
    );

    return {
      success: true,
      challenges: [randomChallenge],
      cached: false,
      quotaRemaining: quota.challengesLimit - quota.challengesGenerated,
    };
  } catch (error: any) {
    console.error("[AIGeneration] ❌ CHALLENGE GENERATION FAILED - Error:", error.message);
    throw new functions.https.HttpsError(
      error.code || "internal",
      error.message || "Failed to generate challenges"
    );
  }
});