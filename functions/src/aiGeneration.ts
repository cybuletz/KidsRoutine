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

   // const userId = context.auth?.uid || "test_user";

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

      // 3. CHECK CACHE
      const cacheKey = `${childAge}_${preferences.join("_")}_gemini`;
      const cachedResult = await db
        .collection("ai_generated_tasks")
        .where("cacheKey", "==", cacheKey)
        .where("expiresAt", ">", admin.firestore.Timestamp.now())
        .limit(count)
        .get();

      if (cachedResult.size > 0) {
        console.log(
          `[AIGeneration] Using ${cachedResult.size} cached task(s)`
        );
        const tasks = cachedResult.docs.map((doc) => doc.data().task);

        quota.tasksGenerated += cachedResult.size;
        await db.collection("ai_quotas").doc(userId).set(quota);

        return {
          success: true,
          tasks: tasks,
          cached: true,
          quotaRemaining: quota.tasksLimit - quota.tasksGenerated,
        };
      }

      // 4. GENERATE NEW TASKS
      const systemPrompt = `You are a children's task generator for ages ${childAge}+.
Generate a fun, engaging task that is age-appropriate and safe.

TASK REQUIREMENTS:
- Title: Short, fun, emoji-enabled
- Description: Clear, child-friendly, under 100 chars
- Duration: 5-60 seconds
- Category: MORNING_ROUTINE, HEALTH, LEARNING, CREATIVE, SOCIAL, EMOTIONAL, REAL_LIFE
- Difficulty: EASY, MEDIUM, HARD
- Type: LOGIC, REAL_LIFE, CREATIVE, LEARNING, EMOTIONAL, CO_OP

${preferences.length > 0 ? `Child prefers: ${preferences.join(", ")}` : ""}
${recentCompletions.length > 0 ? `Recently completed: ${recentCompletions.join(", ")}. Don't repeat similar tasks.` : ""}

Return ONLY valid JSON (no markdown, no extra text):
{
  "title": "string",
  "description": "string",
  "estimatedDurationSec": number,
  "category": "string",
  "difficulty": "string",
  "xpReward": number,
  "type": "string"
}`;

      const prompt =
        "Generate a single engaging task for a child. Make it fun and age-appropriate.";

      const tasks = [];

      for (let i = 0; i < Math.min(count, 3); i++) {
        const response = await callGemini(apiKey, systemPrompt, prompt);

        try {
          // Try to repair incomplete JSON if needed
          let jsonStr = response.trim();

          // If JSON is incomplete, try to close it
          if (!jsonStr.endsWith('}')) {
            // Count braces to understand structure
            const openBraces = (jsonStr.match(/\{/g) || []).length;
            const closeBraces = (jsonStr.match(/\}/g) || []).length;

            if (openBraces > closeBraces) {
              // Add missing closing braces
              for (let j = 0; j < openBraces - closeBraces; j++) {
                jsonStr += '}';
              }
              console.log("[AIGeneration] Repaired incomplete JSON");
            }
          }

          const task = JSON.parse(jsonStr);

          if (!validateTaskSchema(task)) {
            console.warn("[AIGeneration] Invalid task schema, skipping");
            continue;
          }

          const isSafe = await validateTaskSafety(task);
          if (!isSafe) {
            console.warn("[AIGeneration] Task failed safety check, skipping");
            continue;
          }

          tasks.push(task);

          await db.collection("ai_generated_tasks").add({
            task: task,
            provider: "gemini",
            cacheKey: cacheKey,
            familyId: familyId,
            childAge: childAge,
            createdAt: admin.firestore.Timestamp.now(),
            expiresAt: admin.firestore.Timestamp.fromDate(
              new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
            ),
            usageCount: 0,
          });
        } catch (e) {
          console.error(
            "[AIGeneration] Failed to parse task:",
            e,
            "Response length:",
            response.length
          );
          continue;
        }
      }

      if (tasks.length === 0) {
        throw new functions.https.HttpsError(
          "internal",
          "Failed to generate valid tasks"
        );
      }

      quota.tasksGenerated += tasks.length;
      await db.collection("ai_quotas").doc(userId).set(quota);

      await db.collection("ai_usage").add({
        userId: userId,
        familyId: familyId,
        type: "TASK_GENERATION",
        provider: "gemini",
        taskCount: tasks.length,
        timestamp: admin.firestore.Timestamp.now(),
      });

      console.log(
        `[AIGeneration] Generated ${tasks.length} tasks. Quota: ${quota.tasksGenerated}/${quota.tasksLimit}`
      );

      return {
        success: true,
        tasks: tasks,
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

    const userId = context.auth?.uid || "test_user";

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

      // 3. CHECK CACHE
      const cacheKey = `challenge_${childAge}_${goals.join("_")}_gemini`;
      const cachedResult = await db
        .collection("ai_generated_challenges")
        .where("cacheKey", "==", cacheKey)
        .where("expiresAt", ">", admin.firestore.Timestamp.now())
        .limit(count)
        .get();

      if (cachedResult.size > 0) {
        console.log(
          `[AIGeneration] Using ${cachedResult.size} cached challenge(s)`
        );
        const challenges = cachedResult.docs.map((doc) => doc.data().challenge);

        quota.challengesGenerated += cachedResult.size;
        await db.collection("ai_quotas").doc(userId).set(quota);

        return {
          success: true,
          challenges: challenges,
          cached: true,
          quotaRemaining: quota.challengesLimit - quota.challengesGenerated,
        };
      }

      // 4. GENERATE NEW CHALLENGES
      const systemPrompt = `You are a children's challenge (habit) generator for ages ${childAge}+.
Generate a multi-day challenge that builds healthy habits.

CHALLENGE REQUIREMENTS:
- Title: Short, motivating (under 50 chars)
- Description: Clear, achievable (under 100 chars)
- Duration: 3-30 days
- Category: SLEEP, SCREEN_TIME, HEALTH, SOCIAL, LEARNING
- Success condition: Clear, measurable per day

${goals.length > 0 ? `Parent goals: ${goals.join(", ")}` : ""}

Return ONLY valid JSON (no markdown, no extra text):
{
  "title": "string",
  "description": "string",
  "durationDays": number,
  "category": "string",
  "successCondition": "string"
}`;

      const prompt =
        "Generate a single engaging challenge for a child to build healthy habits.";

      const challenges = [];

      for (let i = 0; i < Math.min(count, 2); i++) {
        const response = await callGemini(apiKey, systemPrompt, prompt);

        try {
          // Try to repair incomplete JSON if needed
          let jsonStr = response.trim();

          // If JSON is incomplete, try to close it
          if (!jsonStr.endsWith('}')) {
            // Count braces to understand structure
            const openBraces = (jsonStr.match(/\{/g) || []).length;
            const closeBraces = (jsonStr.match(/\}/g) || []).length;

            if (openBraces > closeBraces) {
              // Add missing closing braces
              for (let j = 0; j < openBraces - closeBraces; j++) {
                jsonStr += '}';
              }
              console.log("[AIGeneration] Repaired incomplete JSON");
            }
          }

          const challenge = JSON.parse(jsonStr);

          if (!validateChallengeSchema(challenge)) {
            console.warn("[AIGeneration] Invalid challenge schema, skipping");
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

          await db.collection("ai_generated_challenges").add({
            challenge: challenge,
            provider: "gemini",
            cacheKey: cacheKey,
            familyId: familyId,
            childAge: childAge,
            createdAt: admin.firestore.Timestamp.now(),
            expiresAt: admin.firestore.Timestamp.fromDate(
              new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
            ),
            usageCount: 0,
          });
        } catch (e) {
          console.error(
            "[AIGeneration] Failed to parse challenge:",
            e,
            "Response length:",
            response.length
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

      quota.challengesGenerated += challenges.length;
      await db.collection("ai_quotas").doc(userId).set(quota);

      await db.collection("ai_usage").add({
        userId: userId,
        familyId: familyId,
        type: "CHALLENGE_GENERATION",
        provider: "gemini",
        count: challenges.length,
        timestamp: admin.firestore.Timestamp.now(),
      });

      console.log(
        `[AIGeneration] Generated ${challenges.length} challenges. Quota: ${quota.challengesGenerated}/${quota.challengesLimit}`
      );

      return {
        success: true,
        challenges: challenges,
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
          temperature: 0.7,
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