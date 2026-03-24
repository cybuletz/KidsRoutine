import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import axios from "axios";

// ─────────────────────────────────────────────────────────────────────────────
// Re-use the same Gemini helper already in aiGeneration.ts (copy is fine here
// because Cloud Functions bundles each file independently).
// ─────────────────────────────────────────────────────────────────────────────

async function getGeminiApiKey(): Promise<string> {
  const doc = await admin.firestore().collection("config").doc("ai_keys").get();
  const key = doc.data()?.gemini_api_key;
  if (!key) throw new Error("Gemini API key not found in Firestore config/ai_keys");
  return key;
}

async function callGemini(systemPrompt: string, userPrompt: string): Promise<string> {
  const apiKey = await getGeminiApiKey();
  const response = await axios.post(
    `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`,
    {
      contents: [
        {
          role: "user",
          parts: [
            { text: `Instructions: ${systemPrompt}` },
            { text: userPrompt },
          ],
        },
      ],
      generationConfig: { maxOutputTokens: 2000, temperature: 0.85, topP: 1.0 },
    },
    { headers: { "Content-Type": "application/json" }, timeout: 30000 }
  );
  return response.data.candidates?.[0]?.content?.parts?.[0]?.text || "";
}

// ─────────────────────────────────────────────────────────────────────────────
// STORY THEMES by age group
// ─────────────────────────────────────────────────────────────────────────────

const STORY_THEMES_BY_AGE: Record<string, string[]> = {
  "5-7":  ["Enchanted Forest", "Dinosaur Land", "Underwater Kingdom", "Space Pups", "Dragon Eggs"],
  "8-10": ["Lost Island", "Robot Rescue", "Jungle Quest", "Time Machine", "Pirate Code"],
  "11-14":["Cyberpunk City", "Ancient Ruins", "Arctic Expedition", "Ninja Academy", "Alien First Contact"],
  "15-18":["Dystopian Survival", "Startup Hero", "Elite Athlete Protocol", "Deep Ocean Research", "Mountain Ascent"],
};

function getStoryThemes(age: number): string[] {
  if (age <= 7)  return STORY_THEMES_BY_AGE["5-7"];
  if (age <= 10) return STORY_THEMES_BY_AGE["8-10"];
  if (age <= 14) return STORY_THEMES_BY_AGE["11-14"];
  return STORY_THEMES_BY_AGE["15-18"];
}

// ─────────────────────────────────────────────────────────────────────────────
// CLOUD FUNCTION: generateStoryTaskAI
// Called from Android via FirebaseFunctions.getHttpsCallable("generateStoryTaskAI")
// Input:  { familyId, childAge, tier }
// Output: { success, arc: StoryArc, cached, quotaRemaining }
// ─────────────────────────────────────────────────────────────────────────────

export const generateStoryTaskAI = functions.https.onCall(async (data: any, context: any) => {
  const db = admin.firestore();

  const userId = context.auth?.uid;
  if (!userId) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated");
  }

  const { familyId, childAge, tier = "FREE" } = data;
  if (!familyId || !childAge) {
    throw new functions.https.HttpsError("invalid-argument", "familyId and childAge required");
  }

  const today = new Date().toISOString().split("T")[0]; // "2026-03-24"

  console.log(`[StoryGen] Generating story arc for age ${childAge}, family ${familyId}`);

  try {
    // ── 1. QUOTA CHECK ──────────────────────────────────────────────────────
    const quotaRef  = db.collection("ai_quotas").doc(userId);
    const quotaSnap = await quotaRef.get();
    const quota     = quotaSnap.data() || { storyArcsGenerated: 0, storyArcsLimit: tier === "FREE" ? 1 : 99 };
    quota.storyArcsGenerated  = quota.storyArcsGenerated  || 0;
    quota.storyArcsLimit      = quota.storyArcsLimit      || (tier === "FREE" ? 1 : 99);

    if (quota.storyArcsGenerated >= quota.storyArcsLimit) {
      console.warn("[StoryGen] Story arc quota exceeded");
      throw new functions.https.HttpsError("resource-exhausted", "Story arc daily quota exceeded");
    }

    // ── 2. CACHE CHECK ──────────────────────────────────────────────────────
    const cacheKey  = `story_${familyId}_${childAge}_${today}`;
    const cacheSnap = await db.collection("ai_story_arcs")
      .where("cacheKey", "==", cacheKey)
      .limit(1)
      .get();

    if (!cacheSnap.empty) {
      const cached = cacheSnap.docs[0].data().arc;
      console.log(`[StoryGen] ✅ Returning cached arc: ${cached.arcTitle}`);
      quota.storyArcsGenerated += 1;
      await quotaRef.set(quota, { merge: true });
      return { success: true, arc: cached, cached: true, quotaRemaining: quota.storyArcsLimit - quota.storyArcsGenerated };
    }

    // ── 3. PICK THEME ────────────────────────────────────────────────────────
    const themes  = getStoryThemes(childAge);
    const theme   = themes[Math.floor(Math.random() * themes.length)];

    // ── 4. BUILD PROMPT ──────────────────────────────────────────────────────
    const systemPrompt = `
You are a creative storyteller for children (age ${childAge}).
Write a 3-day mini adventure story arc.
Each chapter is ONE real-life task the child does that advances the story.
Tasks must be safe, educational, and achievable in under 5 minutes.
Return ONLY valid JSON. No markdown. No extra text.
`.trim();

    const userPrompt = `
Theme: "${theme}"
Child age: ${childAge}

Generate a 3-chapter story arc. Output this EXACT JSON structure:
{
  "arcTitle": "string (max 30 chars, no emoji)",
  "arcEmoji": "single emoji",
  "theme": "${theme}",
  "chapters": [
    {
      "day": 1,
      "chapterTitle": "string (max 30 chars)",
      "narrative": "string (1-sentence story context, max 80 chars)",
      "taskTitle": "emoji + string (max 40 chars)",
      "taskDescription": "string (action-oriented, max 70 chars)",
      "estimatedDurationSec": number (30-300),
      "category": "HEALTH|CREATIVITY|LEARNING|SOCIAL|FAMILY",
      "difficulty": "EASY|MEDIUM|HARD",
      "xpReward": number (20-80),
      "type": "STORY"
    },
    { "day": 2, ... },
    { "day": 3, ... }
  ]
}
`.trim();

    // ── 5. CALL GEMINI ───────────────────────────────────────────────────────
    console.log(`[StoryGen] Calling Gemini with theme: ${theme}`);
    const raw = await callGemini(systemPrompt, userPrompt);
    console.log(`[StoryGen] Raw response (${raw.length} chars): ${raw.substring(0, 200)}`);

    // ── 6. PARSE + VALIDATE ─────────────────────────────────────────────────
    let parsed: any;
    try {
      // Strip any accidental markdown fences
      const clean = raw.replace(/```json|```/g, "").trim();
      parsed = JSON.parse(clean);
    } catch (e) {
      console.error("[StoryGen] JSON parse error:", e);
      throw new functions.https.HttpsError("internal", "Gemini returned invalid JSON");
    }

    if (!parsed.arcTitle || !Array.isArray(parsed.chapters) || parsed.chapters.length !== 3) {
      throw new functions.https.HttpsError("internal", "Story arc schema validation failed");
    }

    // ── 7. BUILD FINAL ARC OBJECT ────────────────────────────────────────────
    const arcId = `arc_${familyId}_${Date.now()}`;
    const arc = {
      arcId,
      arcTitle:   parsed.arcTitle,
      arcEmoji:   parsed.arcEmoji  || "📖",
      theme:      parsed.theme     || theme,
      childAge,
      familyId,
      chapters:   parsed.chapters,
      startDate:  today,
      currentDay: 1,
      isComplete: false,
      createdAt:  Date.now(),
    };

    // ── 8. CACHE IN FIRESTORE ────────────────────────────────────────────────
    await db.collection("ai_story_arcs").doc(arcId).set({
      arc,
      cacheKey,
      familyId,
      childAge,
      createdAt: admin.firestore.Timestamp.now(),
    });

    console.log(`[StoryGen] ✅ Story arc cached: "${arc.arcTitle}" (${arcId})`);

    // ── 9. UPDATE QUOTA ──────────────────────────────────────────────────────
    quota.storyArcsGenerated += 1;
    await quotaRef.set(quota, { merge: true });

    return {
      success: true,
      arc,
      cached: false,
      quotaRemaining: quota.storyArcsLimit - quota.storyArcsGenerated,
    };

  } catch (error: any) {
    console.error("[StoryGen] ❌ Error:", error.message);
    throw new functions.https.HttpsError(error.code || "internal", error.message || "Story generation failed");
  }
});