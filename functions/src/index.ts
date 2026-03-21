import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// ===== TASK COMPLETION NOTIFICATIONS =====

export const notifyTaskCompletion = functions.firestore
  .document("taskProgress/{taskId}")
  .onWrite(async (change: any, context: any) => {
    const newData = change.after.data();
    const oldData = change.before.data();

    // Only trigger when task becomes completed
    if (!newData || !newData.userId || !newData.status) {
      return;
    }

    // Check if task just became COMPLETED
    if (oldData?.status === "COMPLETED" || newData.status !== "COMPLETED") {
      return;
    }

    const userId = newData.userId;
    const taskTitle = newData.taskTitle || "Task";
    const xpGained = newData.xpGained || 0;
    const validationStatus = newData.validationStatus || "";
    const familyId = newData.familyId;

    console.log(`[Task Completion] Task completed by user: ${userId}`);
    console.log(`[Task Completion] Family ID: ${familyId}`);
    console.log(`[Task Completion] Validation status: ${validationStatus}`);

    try {
      // Get user's FCM token
      const userDoc = await db.collection("users").doc(userId).get();
      const userFcmToken = userDoc.data()?.fcmToken;
      const childFamilyId = userDoc.data()?.familyId || familyId;
      const childDisplayName = userDoc.data()?.displayName || "Your child";

      console.log(`[Task Completion] User FCM token: ${userFcmToken ? "found" : "NOT FOUND"}`);
      console.log(`[Task Completion] Child family ID from user doc: ${childFamilyId}`);

      if (!userFcmToken) {
        console.log(`[Task Completion] No FCM token for user: ${userId}`);
        return;
      }

      // Send notification to child
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

      console.log(`[Task Completion] Notification sent to child: ${userId}`);

      // Check if task needs parent approval
      const needsParent = validationStatus === "PENDING";
      console.log(`[Task Completion] Needs parent approval: ${needsParent}`);

      if (!needsParent) {
        console.log(`[Task Completion] Task approved automatically, no parent notification needed`);
        return;
      }

      // Find parents in the same family
      if (!childFamilyId) {
        console.log(`[Task Completion] Child has no family ID: ${userId}`);
        return;
      }

      const parentsSnapshot = await db
        .collection("users")
        .where("familyId", "==", childFamilyId)
        .where("role", "==", "PARENT")
        .get();

      console.log(`[Task Completion] Searching for parents in family: ${childFamilyId}`);
      console.log(`[Task Completion] Found ${parentsSnapshot.size} parents to notify`);

      for (const parentDoc of parentsSnapshot.docs) {
        const parentData = parentDoc.data();
        const parentFcmToken = parentData.fcmToken;

        console.log(`[Task Completion] Parent ${parentDoc.id} FCM token: ${parentFcmToken ? "found" : "NOT FOUND"}`);

        if (!parentFcmToken) {
          console.log(`[Task Completion] Parent has no FCM token: ${parentDoc.id}`);
          continue;
        }

        try {
          await messaging.send({
            token: parentFcmToken,
            notification: {
              title: "Task Needs Approval ⏳",
              body: `${childDisplayName} completed: ${taskTitle}`,
            },
            data: {
              type: "PARENT_APPROVAL_NEEDED",
              childId: userId,
              taskTitle: taskTitle,
            },
            android: {
              priority: "high",
            },
          });

          console.log(`[Task Completion] Approval notification sent to parent: ${parentDoc.id}`);
        } catch (parentError: any) {
          if (parentError?.code === "messaging/registration-token-not-registered") {
            console.log(`[Task Completion] Parent token invalid, deleting it: ${parentDoc.id}`);
            await db.collection("users").doc(parentDoc.id).update({ fcmToken: "" });
          } else {
            console.error(`[Task Completion] Error sending to parent ${parentDoc.id}:`, parentError);
          }
        }
      }
    } catch (error) {
      console.error("[Task Completion] Error:", error);
    }
  });

// ===== FAMILY CHAT NOTIFICATIONS =====

export const notifyFamilyMessage = functions.firestore
  .document("familyMessages/{messageId}")
  .onCreate(async (snap: any, context: any) => {
    const messageData = snap.data();

    const senderId = messageData.senderId;
    const senderName = messageData.senderName || "Family member";
    const messageBody = messageData.body || "";
    const familyId = messageData.familyId;

    console.log(`[Family Chat] New message in family: ${familyId}`);

    try {
      // Get all family members
      const membersSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .get();

      console.log(`[Family Chat] Found ${membersSnapshot.size} family members`);

      // Send notification to all members except sender
      for (const memberDoc of membersSnapshot.docs) {
        if (memberDoc.id === senderId) {
          continue; // Don't notify sender
        }

        const memberData = memberDoc.data();
        const fcmToken = memberData.fcmToken;

        if (!fcmToken) {
          console.log(`[Family Chat] Member has no FCM token: ${memberDoc.id}`);
          continue;
        }

        await messaging.send({
          token: fcmToken,
          notification: {
            title: `💬 ${senderName}`,
            body: messageBody.substring(0, 100),
          },
          data: {
            type: "FAMILY_MESSAGE",
            senderId: senderId,
            messageId: snap.id,
            familyId: familyId,
          },
          android: {
            priority: "high",
          },
        });

        console.log(`[Family Chat] Message notification sent to: ${memberDoc.id}`);
      }
    } catch (error) {
      console.error("[Family Chat] Error:", error);
    }
  });

// ===== ACHIEVEMENT UNLOCK NOTIFICATIONS =====

export const notifyAchievementUnlock = functions.firestore
  .document("users/{userId}")
  .onUpdate(async (change: any, context: any) => {
    const userId = context.params.userId;
    const newData = change.after.data();
    const oldData = change.before.data();

    const newBadges = newData.badges || [];
    const oldBadges = oldData?.badges || [];

    // Check if new badge was added
    if (newBadges.length <= oldBadges.length) {
      return;
    }

    const newBadge = newBadges[newBadges.length - 1];

    console.log(`[Achievement] New badge unlocked by user: ${userId}`);

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      const fcmToken = userDoc.data()?.fcmToken;

      if (!fcmToken) {
        console.log(`[Achievement] No FCM token for user: ${userId}`);
        return;
      }

      await messaging.send({
        token: fcmToken,
        notification: {
          title: "Achievement Unlocked! ⭐",
          body: newBadge.title || "You earned a new badge!",
        },
        data: {
          type: "ACHIEVEMENT_UNLOCKED",
          badgeId: newBadge.id || "",
          badgeTitle: newBadge.title || "",
        },
        android: {
          priority: "high",
        },
      });

      console.log(`[Achievement] Notification sent to user: ${userId}`);
    } catch (error) {
      console.error("[Achievement] Error:", error);
    }
  });

// ===== TASK APPROVAL NOTIFICATIONS =====

export const notifyTaskApproval = functions.firestore
  .document("taskProgress/{taskId}")
  .onUpdate(async (change: any, context: any) => {
    const newData = change.after.data();
    const oldData = change.before.data();

    // Check if approval status changed
    if (oldData?.approvalStatus === newData.approvalStatus) {
      return;
    }

    const childId = newData.userId;
    const taskTitle = newData.taskTitle || "Task";
    const approvalStatus = newData.approvalStatus; // "APPROVED" or "REJECTED"

    console.log(`[Task Approval] Task ${approvalStatus}: ${taskTitle}`);

    try {
      const childDoc = await db.collection("users").doc(childId).get();
      const fcmToken = childDoc.data()?.fcmToken;

      if (!fcmToken) {
        console.log(`[Task Approval] No FCM token for child: ${childId}`);
        return;
      }

      const title =
        approvalStatus === "APPROVED"
          ? "Task Approved! ✅"
          : "Task Needs Revision ❌";
      const body = `${taskTitle} was ${approvalStatus.toLowerCase()}`;

      await messaging.send({
        token: fcmToken,
        notification: {
          title: title,
          body: body,
        },
        data: {
          type: "TASK_APPROVAL",
          taskTitle: taskTitle,
          status: approvalStatus,
        },
        android: {
          priority: "high",
        },
      });

      console.log(`[Task Approval] Notification sent to child: ${childId}`);
    } catch (error) {
      console.error("[Task Approval] Error:", error);
    }
  });

// ===== FAMILY MEMBER ADDED NOTIFICATIONS =====

export const notifyFamilyMemberAdded = functions.firestore
  .document("users/{userId}")
  .onCreate(async (snap: any, context: any) => {
    const newUser = snap.data();
    const userId = context.params.userId;
    const familyId = newUser.familyId;
    const newUserName = newUser.displayName || "New member";

    if (!familyId) {
      return;
    }

    console.log(`[Family Member] New member added to family: ${familyId}`);

    try {
      // Get all family members
      const membersSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .get();

      console.log(`[Family Member] Notifying ${membersSnapshot.size} family members`);

      // Notify all other members
      for (const memberDoc of membersSnapshot.docs) {
        if (memberDoc.id === userId) {
          continue; // Don't notify the new member
        }

        const memberData = memberDoc.data();
        const fcmToken = memberData.fcmToken;

        if (!fcmToken) {
          console.log(`[Family Member] Member has no FCM token: ${memberDoc.id}`);
          continue;
        }

        await messaging.send({
          token: fcmToken,
          notification: {
            title: "Family Update 👨‍👩‍👧‍👦",
            body: `${newUserName} joined your family!`,
          },
          data: {
            type: "FAMILY_MEMBER_ADDED",
            newMemberId: userId,
            newMemberName: newUserName,
          },
          android: {
            priority: "high",
          },
        });

        console.log(`[Family Member] Notification sent to: ${memberDoc.id}`);
      }
    } catch (error) {
      console.error("[Family Member] Error:", error);
    }
  });