import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Approval Notifications
 * Listens to taskProgress updates
 * Notifies child when task is approved or rejected
 */
export const notifyTaskApproval = functions.firestore
  .document("taskProgress/{taskId}")
  .onUpdate(async (change: any, context: any) => {
    const newData = change.after.data();
    const oldData = change.before.data();

    // Check if approval status changed
    if (oldData?.validationStatus === newData.validationStatus) {
      return;
    }

    const childId = newData.userId;
    const taskTitle = newData.taskTitle || "Task";
    const validationStatus = newData.validationStatus; // "APPROVED" or "PENDING"

    console.log(`[TaskApproval] Task ${validationStatus}: ${taskTitle}`);

    try {
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[TaskApproval] Child not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[TaskApproval] No FCM token for child: ${childId}`);
        return;
      }

      const title = validationStatus === "APPROVED"
        ? "Task Approved! ✅"
        : "Task Needs Review ⏳";

      const body = `${taskTitle} was ${validationStatus.toLowerCase()}`;

      await messaging.send({
        token: fcmToken,
        notification: { title, body },
        data: {
          type: "TASK_APPROVAL",
          taskTitle: taskTitle,
          status: validationStatus,
        },
        android: { priority: "high" },
      });

      console.log(`[TaskApproval] Notification sent to child: ${childId}`);
    } catch (error) {
      console.error("[TaskApproval] Error:", error);
    }
  });