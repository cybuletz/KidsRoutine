import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Family Chat Notifications
 * Listens to familyMessages collection creation
 * Notifies all family members except sender
 */
export const notifyFamilyChat = functions.firestore
  .document("familyMessages/{messageId}")
  .onCreate(async (snap: any, context: any) => {
    const messageData = snap.data();

    const senderId = messageData.senderId;
    const senderName = messageData.senderName || "Family member";
    const messageBody = messageData.body || "";
    const familyId = messageData.familyId;

    console.log(`[FamilyChat] New message in family: ${familyId}`);

    try {
      // Get all family members
      const membersSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .get();

      console.log(`[FamilyChat] Found ${membersSnapshot.size} family members`);

      // Send notification to all members except sender
      for (const memberDoc of membersSnapshot.docs) {
        if (memberDoc.id === senderId) {
          continue;
        }

        const memberData = memberDoc.data();
        const fcmToken = memberData.fcmToken;

        if (!fcmToken) {
          console.log(`[FamilyChat] Member has no FCM token: ${memberDoc.id}`);
          continue;
        }

        try {
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
              notification: {
                channelId: "family_chat"
              }
            },
          });

          console.log(`[FamilyChat] Notification sent to: ${memberDoc.id}`);
        } catch (error: any) {
          if (error?.code === "messaging/registration-token-not-registered") {
            console.log(`[FamilyChat] Invalid token for ${memberDoc.id}, clearing...`);
            await db.collection("users").doc(memberDoc.id).update({ fcmToken: "" });
          } else {
            console.error(`[FamilyChat] Error sending to ${memberDoc.id}:`, error);
          }
        }
      }
    } catch (error) {
      console.error("[FamilyChat] Error:", error);
    }
  });