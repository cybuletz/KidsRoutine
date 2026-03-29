import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Update Notifications
 * Listens to global tasks collection updates
 * Syncs to family-scoped path and notifies children
 */
export const notifyTaskUpdate = functions.firestore
  .document('tasks/{taskId}')
  .onUpdate(async (change, context) => {
    const taskId = context.params.taskId;
    const afterData = change.after.data();

    console.log(`[TaskUpdate] TRIGGERED for taskId: ${taskId}`);

    try {
      const assignmentsSnapshot = await db
        .collection('taskAssignments')
        .where('taskId', '==', taskId)
        .get();

      console.log(`[TaskUpdate] Found ${assignmentsSnapshot.size} assignments`);

      if (assignmentsSnapshot.empty) {
        console.log("[TaskUpdate] No assignments found");
        return;
      }

      // Mark all assignments as modified to trigger UI refresh
      const batch = db.batch();
      const familyId = afterData.familyId || "unknown";

      for (const doc of assignmentsSnapshot.docs) {
        batch.update(doc.ref, {
          taskUpdatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }
      await batch.commit();
      console.log("[TaskUpdate] All assignments marked as modified");

      // Sync to family-scoped path if familyId exists
      if (familyId !== "unknown") {
        try {
          await db
            .collection("families").doc(familyId)
            .collection("tasks").doc(taskId)
            .update({
              ...afterData,
              updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
          console.log(`[TaskUpdate] Synced to family-scoped path: ${familyId}/tasks/${taskId}`);
        } catch (e: any) {
          console.warn(`[TaskUpdate] Could not sync to family path: ${e.message}`);
        }
      }

      // Send FCM notifications to children
      for (const assignmentDoc of assignmentsSnapshot.docs) {
        const assignment = assignmentDoc.data();
        const childId = assignment.childId;

        try {
          const childDoc = await db.collection("users").doc(childId).get();
          const fcmToken = childDoc.data()?.fcmToken;

          if (!fcmToken) {
            console.log(`[TaskUpdate] No FCM token for child: ${childId}`);
            continue;
          }

          await messaging.send({
            token: fcmToken,
            notification: {
              title: `📋 Task Updated: ${afterData.title}`,
              body: 'A task has been updated'
            },
            data: {
              type: "TASK_UPDATED",
              taskId: taskId,
              userId: childId,
              familyId: familyId,
              refreshTrigger: "true"
            },
            android: { priority: "high" }
          });

          console.log(`[TaskUpdate] FCM sent to child: ${childId}`);
        } catch (error: any) {
          if (error?.code === "messaging/registration-token-not-registered") {
            await db.collection("users").doc(childId).update({ fcmToken: "" });
          }
        }
      }

    } catch (error) {
      console.error("[TaskUpdate] Error:", error);
    }
  });

/**
 * Family-Scoped Task Instance Updates
 * Listens to families/{familyId}/users/{userId}/task_instances/ status changes
 */
export const notifyTaskInstanceUpdateFamilyScoped = functions.firestore
  .document("families/{familyId}/users/{userId}/task_instances/{instanceId}")
  .onUpdate(async (change: any, context: any) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();
    const { familyId, userId, instanceId } = context.params;

    if (beforeData.status === "COMPLETED" || afterData.status !== "COMPLETED") {
      return;
    }

    console.log(`[TaskInstanceUpdate-FamilyScoped] Task instance ${instanceId} marked COMPLETED for user ${userId}`);

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) return;

      const userFcmToken = userDoc.data()?.fcmToken;
      if (!userFcmToken) return;

      await messaging.send({
        token: userFcmToken,
        notification: {
          title: "Task Status Updated ✅",
          body: "Your task is now marked complete!",
        },
        data: {
          type: "TASK_STATUS_UPDATED",
          userId: userId,
          familyId: familyId,
          instanceId: instanceId,
          refreshTrigger: "true",
        },
        android: { priority: "high" },
      });

      console.log(`[TaskInstanceUpdate-FamilyScoped] Notification sent`);
    } catch (error: any) {
      console.warn(`[TaskInstanceUpdate-FamilyScoped] Error: ${error.message}`);
    }
  });