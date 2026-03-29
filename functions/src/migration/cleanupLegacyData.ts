import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();

/**
 * Cleanup Legacy Data
 * HTTPS callable function
 * Deletes all old/global collections to prepare for family-scoped migration
 *
 * ⚠️ WARNING: This DELETES data. Use only when starting fresh.
 *
 * Usage: firebase functions:shell
 *        > cleanupLegacyData()
 */
export const cleanupLegacyData = functions.https.onCall(async (data, context) => {
  // Only allow authenticated users
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Must be signed in");
  }

  console.log("[Migration] Starting legacy data cleanup...");
  console.log("[Migration] ⚠️  WARNING: This will DELETE all old data");

  try {
    let deletedCount = 0;

    // ✅ DELETE: /tasks collection (global)
    console.log("[Migration] Deleting global tasks collection...");
    const tasksSnapshot = await db.collection("tasks").get();
    let tasksDeleted = 0;

    const tasksBatch = db.batch();
    tasksSnapshot.docs.forEach((doc) => {
      tasksBatch.delete(doc.ref);
      tasksDeleted++;
    });

    if (tasksDeleted > 0) {
      await tasksBatch.commit();
      deletedCount += tasksDeleted;
      console.log(`[Migration] ✅ Deleted ${tasksDeleted} global tasks`);
    }

    // ✅ DELETE: /taskProgress collection (global)
    console.log("[Migration] Deleting global taskProgress collection...");
    const progressSnapshot = await db.collection("taskProgress").get();
    let progressDeleted = 0;

    const progressBatch = db.batch();
    progressSnapshot.docs.forEach((doc) => {
      progressBatch.delete(doc.ref);
      progressDeleted++;
    });

    if (progressDeleted > 0) {
      await progressBatch.commit();
      deletedCount += progressDeleted;
      console.log(`[Migration] ✅ Deleted ${progressDeleted} taskProgress entries`);
    }

    // ✅ DELETE: /taskAssignments collection (global)
    console.log("[Migration] Deleting global taskAssignments collection...");
    const assignmentsSnapshot = await db.collection("taskAssignments").get();
    let assignmentsDeleted = 0;

    const assignmentsBatch = db.batch();
    assignmentsSnapshot.docs.forEach((doc) => {
      assignmentsBatch.delete(doc.ref);
      assignmentsDeleted++;
    });

    if (assignmentsDeleted > 0) {
      await assignmentsBatch.commit();
      deletedCount += assignmentsDeleted;
      console.log(`[Migration] ✅ Deleted ${assignmentsDeleted} taskAssignments`);
    }

    // ✅ DELETE: /task_instances collection (global) - if it exists
    console.log("[Migration] Deleting global task_instances collection...");
    const instancesSnapshot = await db.collection("task_instances").get();
    let instancesDeleted = 0;

    const instancesBatch = db.batch();
    instancesSnapshot.docs.forEach((doc) => {
      instancesBatch.delete(doc.ref);
      instancesDeleted++;
    });

    if (instancesDeleted > 0) {
      await instancesBatch.commit();
      deletedCount += instancesDeleted;
      console.log(`[Migration] ✅ Deleted ${instancesDeleted} task_instances`);
    }

    // ✅ DELETE: /challenges collection (global) - if it exists
    console.log("[Migration] Deleting global challenges collection...");
    const challengesSnapshot = await db.collection("challenges").get();
    let challengesDeleted = 0;

    const challengesBatch = db.batch();
    challengesSnapshot.docs.forEach((doc) => {
      challengesBatch.delete(doc.ref);
      challengesDeleted++;
    });

    if (challengesDeleted > 0) {
      await challengesBatch.commit();
      deletedCount += challengesDeleted;
      console.log(`[Migration] ✅ Deleted ${challengesDeleted} challenges`);
    }

    // ✅ DELETE: Family-scoped task_instances (if app created any before migration)
    console.log("[Migration] Deleting family-scoped task_instances...");
    const familiesSnapshot = await db.collection("families").get();
    let familyInstancesDeleted = 0;

    for (const familyDoc of familiesSnapshot.docs) {
      const familyId = familyDoc.id;
      const usersSnapshot = await db
        .collection("families")
        .doc(familyId)
        .collection("users")
        .get();

      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const instancesSnap = await db
          .collection("families")
          .doc(familyId)
          .collection("users")
          .doc(userId)
          .collection("task_instances")
          .get();

        const instancesBatch2 = db.batch();
        instancesSnap.docs.forEach((doc) => {
          instancesBatch2.delete(doc.ref);
          familyInstancesDeleted++;
        });

        if (familyInstancesDeleted > 0) {
          await instancesBatch2.commit();
        }
      }
    }

    if (familyInstancesDeleted > 0) {
      deletedCount += familyInstancesDeleted;
      console.log(`[Migration] ✅ Deleted ${familyInstancesDeleted} family-scoped task_instances`);
    }

    // ✅ DELETE: Family-scoped task_progress (if app created any before migration)
    console.log("[Migration] Deleting family-scoped task_progress...");
    let familyProgressDeleted = 0;

    for (const familyDoc of familiesSnapshot.docs) {
      const familyId = familyDoc.id;
      const usersSnapshot = await db
        .collection("families")
        .doc(familyId)
        .collection("users")
        .get();

      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const progressSnap = await db
          .collection("families")
          .doc(familyId)
          .collection("users")
          .doc(userId)
          .collection("task_progress")
          .get();

        const progressBatch2 = db.batch();
        progressSnap.docs.forEach((doc) => {
          progressBatch2.delete(doc.ref);
          familyProgressDeleted++;
        });

        if (familyProgressDeleted > 0) {
          await progressBatch2.commit();
        }
      }
    }

    if (familyProgressDeleted > 0) {
      deletedCount += familyProgressDeleted;
      console.log(`[Migration] ✅ Deleted ${familyProgressDeleted} family-scoped task_progress`);
    }

    // ✅ PRESERVE: User accounts, families, and settings
    console.log("[Migration] ℹ️  PRESERVED: User accounts, families, family members");

    // ✅ SUCCESS
    console.log("[Migration] ============================================");
    console.log("[Migration] ✅ CLEANUP COMPLETE");
    console.log("[Migration] ============================================");

    return {
      success: true,
      deleted: {
        globalTasks: tasksDeleted,
        globalTaskProgress: progressDeleted,
        globalTaskAssignments: assignmentsDeleted,
        globalTaskInstances: instancesDeleted,
        globalChallenges: challengesDeleted,
        familyScopedTaskInstances: familyInstancesDeleted,
        familyScopedTaskProgress: familyProgressDeleted,
      },
      totalDeleted: deletedCount,
      preserved: {
        users: "✅ All user accounts preserved",
        families: "✅ All families preserved",
        familyMembers: "✅ All family memberships preserved",
      },
      message: "Legacy data cleaned up. Ready for fresh start with family-scoped tasks.",
      timestamp: new Date().toISOString(),
    };

  } catch (error: any) {
    console.error("[Migration] ❌ Error during cleanup:", error);
    throw new functions.https.HttpsError("internal", `Cleanup failed: ${error.message}`);
  }
});

/**
 * Delete Everything (Nuclear Option)
 * ⚠️⚠️⚠️ WARNING: Deletes ALL data including users and families
 * Only use for complete app reset
 */
export const deleteAllData = functions.https.onCall(async (data, context) => {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError("unauthenticated", "Must be signed in");
  }

  // Require confirmation
  if (data?.confirm !== "DELETE_ALL_DATA_NOW") {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "You must pass confirm: 'DELETE_ALL_DATA_NOW' to proceed"
    );
  }

  console.log("[Migration] ⚠️⚠️⚠️ DELETING ALL DATA ⚠️⚠️⚠️");

  try {
    const collections = [
      "users",
      "families",
      "tasks",
      "taskProgress",
      "taskAssignments",
      "task_instances",
      "challenges",
      "familyMessages",
      "leaderboard_snapshots",
      "ai_quotas",
      "ai_cache",
    ];

    let totalDeleted = 0;

    for (const collectionName of collections) {
      console.log(`[Migration] Deleting collection: ${collectionName}`);
      const snapshot = await db.collection(collectionName).get();
      let count = 0;

      const batch = db.batch();
      snapshot.docs.forEach((doc) => {
        batch.delete(doc.ref);
        count++;
      });

      if (count > 0) {
        await batch.commit();
        totalDeleted += count;
        console.log(`[Migration] ✅ Deleted ${count} documents from ${collectionName}`);
      }
    }

    console.log("[Migration] ============================================");
    console.log("[Migration] ✅ ALL DATA DELETED");
    console.log("[Migration] ============================================");

    return {
      success: true,
      totalDeleted: totalDeleted,
      message: "⚠️ All data has been permanently deleted",
      timestamp: new Date().toISOString(),
    };

  } catch (error: any) {
    console.error("[Migration] ❌ Error during deletion:", error);
    throw new functions.https.HttpsError("internal", `Deletion failed: ${error.message}`);
  }
});