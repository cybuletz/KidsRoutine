import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Deletion Notifications
 * Listens to taskAssignments deletions
 * Notifies child that task was removed
 */
export const notifyTaskDeletion = functions.firestore
  .document("taskAssignments/{docId}")
  .onDelete(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const taskId = assignment.taskId;
    const familyId = assignment.familyId;

    console.log(`[TaskDeletion] Assignment deleted for child: ${childId}, taskId: ${taskId}`);

    if (!childId || !taskId) {
      console.warn(`[TaskDeletion] Missing childId or taskId`);
      return;
    }

    try {
      // Try family-scoped path FIRST, then fall back to global
      let taskTitle = "Task";
      try {
        const familyTaskDoc = await db
          .collection("families").doc(familyId)
          .collection("tasks").doc(taskId)
          .get();

        if (familyTaskDoc.exists) {
          taskTitle = familyTaskDoc.data()?.title || "Task";
          console.log(`[TaskDeletion] Found task in family path: ${taskTitle}`);
        } else {
          const globalTaskDoc = await db.collection("tasks").doc(taskId).get();
          if (globalTaskDoc.exists) {
            taskTitle = globalTaskDoc.data()?.title || "Task";
            console.log(`[TaskDeletion] Found task in global path: ${taskTitle}`);
          }
        }
      } catch (e: any) {
        console.log(`[TaskDeletion] Could not fetch task details: ${e.message}`);
      }

      // Get child's FCM token
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[TaskDeletion] Child not found: ${childId}`);
        return;
      }

      const childFcmToken = childDoc.data()?.fcmToken;
      if (!childFcmToken) {
        console.log(`[TaskDeletion] No FCM token for child: ${childId}`);
        return;
      }

      // Notify child
      await messaging.send({
        token: childFcmToken,
        notification: {
          title: "📋 Task Removed",
          body: `"${taskTitle}" has been deleted`,
        },
        data: {
          type: "TASK_DELETED",
          userId: childId,
          taskId: taskId,
          familyId: familyId,
          refreshTrigger: "true",
        },
        android: { priority: "high" },
      });

      console.log(`[TaskDeletion] Notification sent to child: ${childId}`);

    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        console.log(`[TaskDeletion] Invalid token, clearing: ${childId}`);
        await db.collection("users").doc(childId).update({ fcmToken: "" });
      } else {
        console.error(`[TaskDeletion] Error: ${error.code}`);
      }
    }
  });