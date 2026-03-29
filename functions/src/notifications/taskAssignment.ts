import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Assignment Notifications
 * Listens to taskAssignments creation
 * Notifies child of new task
 */
export const notifyTaskAssignment = functions.firestore
  .document("taskAssignments/{docId}")
  .onCreate(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const taskId = assignment.taskId;
    const familyId = assignment.familyId;

    console.log(`[TaskAssignment] Task assigned to child: ${childId}`);

    try {
      // Try family-scoped path FIRST, then global
      let task = null;
      try {
        const familyTaskDoc = await db
          .collection("families").doc(familyId)
          .collection("tasks").doc(taskId)
          .get();

        if (familyTaskDoc.exists) {
          task = familyTaskDoc.data();
        } else {
          const globalTaskDoc = await db.collection("tasks").doc(taskId).get();
          if (globalTaskDoc.exists) {
            task = globalTaskDoc.data();
          }
        }
      } catch (e: any) {
        console.log(`[TaskAssignment] Could not fetch task: ${e.message}`);
      }

      if (!task) {
        console.log(`[TaskAssignment] Task not found: ${taskId}`);
        return;
      }

      // Get child's FCM token
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[TaskAssignment] Child not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[TaskAssignment] No FCM token for child: ${childId}`);
        return;
      }

      // Send notification
      await messaging.send({
        token: fcmToken,
        notification: {
          title: `📋 New Task: ${task.title}`,
          body: task.description || "A new task has been assigned to you",
        },
        data: {
          type: "TASK_ASSIGNED",
          userId: childId,
          taskId: taskId,
          refreshTrigger: "true"
        },
        android: { priority: "high" },
      });

      console.log(`[TaskAssignment] Notification sent to child: ${childId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        await db.collection("users").doc(childId).update({ fcmToken: "" });
      } else {
        console.error(`[TaskAssignment] Error:`, error);
      }
    }
  });