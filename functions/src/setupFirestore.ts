import * as admin from "firebase-admin";

const db = admin.firestore();

/**
 * Initialize Firestore collections for AI generation system
 * Run this ONCE when setting up the project
 *
 * Usage: firebase functions:shell
 *        > setupFirestore()
 */
export async function setupFirestore() {
  try {
    console.log("🔧 Starting Firestore setup...\n");

    // ===== 1. AI CONFIG COLLECTION =====
    console.log("📝 Creating ai_config collection...");

    const aiConfigData = {
      provider: "openai",  // Default provider
      updatedAt: admin.firestore.Timestamp.now(),
      availableProviders: ["openai", "gemini"],
      description: "Active AI provider configuration"
    };

    await db.collection("ai_config").doc("active_provider").set(aiConfigData);
    console.log("✅ ai_config/active_provider created\n");

    // ===== 2. AI QUOTAS COLLECTION =====
    console.log("📝 Creating ai_quotas collection (sample user)...");

    // Create a sample quota document (will be created per-user on first generation)
    const sampleQuotaData = {
      userId: "sample_user_123",
      tier: "FREE",
      tasksGenerated: 0,
      tasksLimit: 1,
      challengesGenerated: 0,
      challengesLimit: 0,
      resetDate: admin.firestore.Timestamp.now(),
      description: "Sample quota document. Real ones created automatically per user."
    };

    await db.collection("ai_quotas").doc("sample_user_123").set(sampleQuotaData);
    console.log("✅ ai_quotas/sample_user_123 created\n");

    // ===== 3. AI CACHE COLLECTION =====
    console.log("📝 Creating ai_cache collection...");

    const cacheSampleData = {
      prompt: "Generate a morning task for 7-year-old",
      result: "Sample cached result",
      type: "TASK",
      timestamp: admin.firestore.Timestamp.now(),
      expiresAt: admin.firestore.Timestamp.fromDate(
        new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
      ),
      description: "Sample cache entry. Real ones created automatically."
    };

    await db.collection("ai_cache").doc("sample_cache_1").set(cacheSampleData);
    console.log("✅ ai_cache/sample_cache_1 created\n");

    // ===== 4. AI GENERATED TASKS COLLECTION =====
    console.log("📝 Creating ai_generated_tasks collection...");

    const generatedTaskData = {
      task: {
        title: "🎨 Paint a Rainbow",
        description: "Use colors to paint a beautiful rainbow",
        estimatedDurationSec: 30,
        category: "CREATIVE",
        difficulty: "EASY",
        xpReward: 15,
        type: "CREATIVE"
      },
      provider: "openai",
      cacheKey: "7_creative_openai",
      familyId: "sample_family",
      childAge: 7,
      createdAt: admin.firestore.Timestamp.now(),
      expiresAt: admin.firestore.Timestamp.fromDate(
        new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
      ),
      usageCount: 0,
      description: "Sample generated task"
    };

    await db.collection("ai_generated_tasks").add(generatedTaskData);
    console.log("✅ ai_generated_tasks sample document created\n");

    // ===== 5. AI GENERATED CHALLENGES COLLECTION =====
    console.log("📝 Creating ai_generated_challenges collection...");

    const generatedChallengeData = {
      challenge: {
        title: "🌙 Sleep Champion",
        description: "Go to bed before 9 PM every night",
        durationDays: 7,
        category: "SLEEP",
        successCondition: "In bed by 9 PM"
      },
      provider: "openai",
      cacheKey: "challenge_7_sleep_openai",
      familyId: "sample_family",
      childAge: 7,
      createdAt: admin.firestore.Timestamp.now(),
      expiresAt: admin.firestore.Timestamp.fromDate(
        new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      ),
      usageCount: 0,
      description: "Sample generated challenge"
    };

    await db.collection("ai_generated_challenges").add(generatedChallengeData);
    console.log("✅ ai_generated_challenges sample document created\n");

    // ===== 6. AI USAGE ANALYTICS COLLECTION =====
    console.log("📝 Creating ai_usage collection...");

    const usageData = {
      userId: "sample_user_123",
      familyId: "sample_family",
      type: "TASK_GENERATION",
      provider: "openai",
      count: 1,
      timestamp: admin.firestore.Timestamp.now(),
      description: "Sample usage analytics"
    };

    await db.collection("ai_usage").add(usageData);
    console.log("✅ ai_usage sample document created\n");

    // ===== 7. CREATE FIRESTORE INDEXES (OPTIONAL) =====
    console.log("📝 Setting up Firestore security rules...\n");

    console.log("✅ ============================================");
    console.log("✅  FIRESTORE SETUP COMPLETE!");
    console.log("✅ ============================================\n");

    console.log("📋 Collections created:");
    console.log("   ✓ ai_config");
    console.log("   ✓ ai_quotas");
    console.log("   ✓ ai_cache");
    console.log("   ✓ ai_generated_tasks");
    console.log("   ✓ ai_generated_challenges");
    console.log("   ✓ ai_usage\n");

    console.log("🔐 IMPORTANT: Update Firestore Security Rules!");
    console.log("   See setupFirestoreRules() for rules code.\n");

    console.log("🔑 IMPORTANT: Set environment variables in Firebase!");
    console.log("   OPENAI_API_KEY=sk-...");
    console.log("   GEMINI_API_KEY=AIza...\n");

    return {
      success: true,
      message: "Firestore setup complete",
      collectionsCreated: 6
    };
  } catch (error) {
    console.error("❌ Setup failed:", error);
    throw error;
  }
}

/**
 * Firestore Security Rules for AI system
 * Copy this into Firebase Console → Firestore → Rules
 */
export function setupFirestoreRules(): string {
  return `
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ===== AI CONFIG (read-only for authenticated users) =====
    match /ai_config/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;  // Admin only
    }

    // ===== AI QUOTAS (users can only read their own) =====
    match /ai_quotas/{userId} {
      allow read: if request.auth.uid == userId;
      allow write: if request.auth.uid == userId || isCloudFunction();
    }

    // ===== AI CACHE (public read, cloud function write) =====
    match /ai_cache/{document=**} {
      allow read: if request.auth != null;
      allow write: if isCloudFunction();
    }

    // ===== AI GENERATED TASKS (public read, cloud function write) =====
    match /ai_generated_tasks/{document=**} {
      allow read: if request.auth != null;
      allow write: if isCloudFunction();
    }

    // ===== AI GENERATED CHALLENGES (public read, cloud function write) =====
    match /ai_generated_challenges/{document=**} {
      allow read: if request.auth != null;
      allow write: if isCloudFunction();
    }

    // ===== AI USAGE (cloud function write only) =====
    match /ai_usage/{document=**} {
      allow read: if false;  // Admin only via Cloud Functions
      allow write: if isCloudFunction();
    }

    // ===== HELPER FUNCTIONS =====
    function isCloudFunction() {
      return request.auth.uid != null &&
             request.auth.token.aud == "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit";
    }
  }
}
  `.trim();
}