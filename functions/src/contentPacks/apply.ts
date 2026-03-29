import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();

/**
 * Apply Content Pack
 * HTTPS callable function
 * Seeds content pack tasks into family task pool after unlock
 */
export const applyContentPack = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be signed in.");
  }

  const { userId, familyId, packId } = data as {
    userId: string;
    familyId: string;
    packId: string;
  };

  if (!userId || !familyId || !packId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "userId, familyId, packId required."
    );
  }

  console.log(`[ContentPack] Applying pack ${packId} to family ${familyId}`);

  try {
    // 1. Load the content pack's task definitions
    const packDoc = await db.collection("content_packs").doc(packId).get();
    if (!packDoc.exists) {
      throw new functions.https.HttpsError(
        "not-found",
        `Content pack '${packId}' not found.`
      );
    }

    const packData = packDoc.data()!;
    const taskTemplates: any[] = packData.tasks || [];

    if (taskTemplates.length === 0) {
      console.log(`[ContentPack] Pack ${packId} has no tasks.`);
      return { seeded: 0 };
    }

    // 2. Write each task into families/{familyId}/tasks
    const batch = db.batch();
    const familyTasksRef = db.collection("families").doc(familyId).collection("tasks");

    taskTemplates.forEach((template: any) => {
      const taskRef = familyTasksRef.doc(); // auto-ID
      batch.set(taskRef, {
        ...template,
        taskId:    taskRef.id,
        familyId:  familyId,
        createdBy: userId,
        source:    "content_pack",
        packId:    packId,
        isActive:  true,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });
    });

    await batch.commit();
    console.log(`[ContentPack] ✅ Seeded ${taskTemplates.length} tasks from pack '${packId}' into family '${familyId}'`);

    return { seeded: taskTemplates.length };

  } catch (error: any) {
    console.error("[ContentPack] Error:", error);
    throw new functions.https.HttpsError(
      "internal",
      error.message || "Failed to apply content pack"
    );
  }
});