import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Task Completion Notifications
 * Listens to global taskProgress collection (legacy)
 * Notifies child + parents
 */
export const notifyTaskCompletion = functions.firestore
  .document("taskProgress/{taskId}")
  .onCreate(async (snap: any, context: any) => {
    const newData = snap.data();

    if (!newData || !newData.userId || !newData.status) {
      console.log("[TaskCompletion] Missing required fields");
      return;
    }

    if (newData.status !== "COMPLETED") {
      console.log("[TaskCompletion] Task not completed, status:", newData.status);
      return;
    }

    const userId = newData.userId;
    const taskTitle = newData.taskTitle || "Task";
    const xpGained = newData.xpGained || 0;
    const validationStatus = newData.validationStatus || "";
    const taskInstanceId = newData.taskInstanceId || "";

    console.log(`[TaskCompletion] Task completed by user: ${userId}`);

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        console.log(`[TaskCompletion] User not found: ${userId}`);
        return;
      }

      const userData = userDoc.data();
      const userFcmToken = userData?.fcmToken;
      const familyId = userData?.familyId;
      const childDisplayName = userData?.displayName || "Your child";

      if (!userFcmToken) {
        console.log(`[TaskCompletion] No FCM token for user: ${userId}`);
        return;
      }

      // Notify child
      try {
        await messaging.send({
          token: userFcmToken,
          notification: {
            title: "Task Completed! 🎉",
            body: `${taskTitle} - You earned ${xpGained} XP!`,
          },
          data: {
            type: "TASK_COMPLETION",
            userId: userId,
          },
          android: {
            priority: "high",
          },
        });
        console.log(`[TaskCompletion] Notification sent to child: ${userId}`);
      } catch (error: any) {
        if (error?.code === "messaging/registration-token-not-registered") {
          console.log(`[TaskCompletion] Invalid token, clearing: ${userId}`);
          await db.collection("users").doc(userId).update({ fcmToken: "" });
        }
        return;
      }

      // Notify parents
      if (!familyId) {
        console.log(`[TaskCompletion] No family ID for user: ${userId}`);
        return;
      }

      const parentsSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .where("role", "==", "PARENT")
        .get();

      for (const parentDoc of parentsSnapshot.docs) {
        const parentData = parentDoc.data();
        const parentFcmToken = parentData.fcmToken;

        if (!parentFcmToken) continue;

        try {
          const title = validationStatus === "PENDING"
            ? "Task Needs Approval ⏳"
            : "Task Completed ✅";

          const body = validationStatus === "PENDING"
            ? `${childDisplayName} completed: ${taskTitle}`
            : `${childDisplayName} completed: ${taskTitle} (Auto-approved)`;

          await messaging.send({
            token: parentFcmToken,
            notification: { title, body },
            data: {
              type: validationStatus === "PENDING" ? "PARENT_APPROVAL_NEEDED" : "TASK_COMPLETION",
              childId: userId,
              taskTitle: taskTitle,
              status: validationStatus,
            },
            android: { priority: "high" },
          });

          console.log(`[TaskCompletion] Parent notification sent: ${parentDoc.id}`);
        } catch (parentError: any) {
          if (parentError?.code === "messaging/registration-token-not-registered") {
            await db.collection("users").doc(parentDoc.id).update({ fcmToken: "" });
          }
        }
      }

      // Track completions
      await db.collection("users").doc(userId).update({
        tasksCompleted: admin.firestore.FieldValue.increment(1)
      });

    } catch (error) {
      console.error("[TaskCompletion] Error:", error);
    }
  });

/**
 * Family-Scoped Task Completion Notifications
 * Listens to families/{familyId}/users/{userId}/task_progress/
 */
export const notifyTaskCompletionFamilyScoped = functions.firestore
  .document("families/{familyId}/users/{userId}/task_progress/{docId}")
  .onCreate(async (snap: any, context: any) => {
    const newData = snap.data();
    const { familyId, userId } = context.params;

    if (!newData || !userId || !newData.status) {
      console.log("[TaskCompletion-FamilyScoped] Missing required fields");
      return;
    }

    if (newData.status !== "COMPLETED") {
      console.log("[TaskCompletion-FamilyScoped] Task not completed, status:", newData.status);
      return;
    }

    const taskTitle = newData.taskTitle || "Task";
    const xpGained = newData.xpGained || 0;
    const validationStatus = newData.validationStatus || "";

    console.log(`[TaskCompletion-FamilyScoped] Task completed by user: ${userId} in family: ${familyId}`);

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        console.log(`[TaskCompletion-FamilyScoped] User not found: ${userId}`);
        return;
      }

      const userData = userDoc.data();
      const userFcmToken = userData?.fcmToken;
      const childDisplayName = userData?.displayName || "Your child";

      if (!userFcmToken) {
        console.log(`[TaskCompletion-FamilyScoped] No FCM token for user: ${userId}`);
        return;
      }

      // Notify child
      try {
        await messaging.send({
          token: userFcmToken,
          notification: {
            title: "Task Completed! 🎉",
            body: `${taskTitle} - You earned ${xpGained} XP!`,
          },
          data: {
            type: "TASK_COMPLETION",
            userId: userId,
            familyId: familyId,
          },
          android: { priority: "high" },
        });
        console.log(`[TaskCompletion-FamilyScoped] FCM sent to child: ${userId}`);
      } catch (error: any) {
        if (error?.code === "messaging/registration-token-not-registered") {
          await db.collection("users").doc(userId).update({ fcmToken: "" });
        }
      }

      // Notify parents
      try {
        const familySnapshot = await db
          .collection("users")
          .where("familyId", "==", familyId)
          .where("role", "==", "PARENT")
          .get();

        for (const parentDoc of familySnapshot.docs) {
          const parentFcmToken = parentDoc.data()?.fcmToken;
          if (!parentFcmToken) continue;

          await messaging.send({
            token: parentFcmToken,
            notification: {
              title: `✅ ${childDisplayName} Completed a Task!`,
              body: `"${taskTitle}" completed with status: ${validationStatus}`,
            },
            data: {
              type: "CHILD_TASK_COMPLETED",
              childId: userId,
              familyId: familyId,
            },
            android: { priority: "high" },
          });
          console.log(`[TaskCompletion-FamilyScoped] Parent notification sent`);
        }
      } catch (error: any) {
        console.warn(`[TaskCompletion-FamilyScoped] Could not notify parents: ${error.message}`);
      }

    } catch (error: any) {
      console.error(`[TaskCompletion-FamilyScoped] Error: ${error.message}`);
    }
  });