import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Update Notifications
 * Listens to family-scoped task updates
 * Touches all assignments in that family to trigger UI refresh
 */
export const notifyTaskUpdate = functions.firestore
  .document('families/{familyId}/tasks/{taskId}')
  .onUpdate(async (change, context) => {
    const taskId = context.params.taskId;
    const familyId = context.params.familyId;
    const afterData = change.after.data();

    console.log(`[TaskUpdate] TRIGGERED for taskId: ${taskId} in family: ${familyId}`);

    try {
      // ✅ Query all users in this family (use .doc() not .document())
      const usersSnapshot = await db
        .collection('families').doc(familyId)
        .collection('users')
        .get();

      console.log(`[TaskUpdate] Found ${usersSnapshot.size} users in family`);

      let touchedCount = 0;
      const batch = db.batch();

      // ✅ For each user, find and touch their assignments for this task
      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const assignmentsSnapshot = await db
          .collection('families').doc(familyId)
          .collection('users').doc(userId)
          .collection('assignments')
          .where('taskId', '==', taskId)
          .get();

        for (const assignmentDoc of assignmentsSnapshot.docs) {
          batch.update(assignmentDoc.ref, {
            taskUpdatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          touchedCount++;
        }
      }

      // ✅ Commit batch updates
      if (touchedCount > 0) {
        await batch.commit();
        console.log(`[TaskUpdate] ✅ Touched ${touchedCount} assignments`);
      }

      // ✅ Send FCM notifications to all children who have this task assigned
      const childrenSet = new Set<string>();

      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;
        const assignmentsSnapshot = await db
          .collection('families').doc(familyId)
          .collection('users').doc(userId)
          .collection('assignments')
          .where('taskId', '==', taskId)
          .get();

        if (!assignmentsSnapshot.empty) {
          childrenSet.add(userId);
        }
      }

      console.log(`[TaskUpdate] Notifying ${childrenSet.size} children`);

      for (const childId of childrenSet) {
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
            console.log(`[TaskUpdate] Invalid FCM token for ${childId}, clearing...`);
            await db.collection("users").doc(childId).update({ fcmToken: "" });
          } else {
            console.error(`[TaskUpdate] Error sending to ${childId}:`, error.message);
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

    // Only trigger if task just became COMPLETED
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