import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Family Member Added Notifications
 * Listens to users collection creation
 * Notifies all existing family members when new member joins
 */
export const notifyFamilyMemberAdded = functions.firestore
  .document("users/{userId}")
  .onCreate(async (snap: any, context: any) => {
    const newUser = snap.data();
    const userId = context.params.userId;
    const familyId = newUser.familyId;
    const newUserName = newUser.displayName || "New member";

    if (!familyId) {
      console.log("[FamilyMember] No family ID for new user");
      return;
    }

    console.log(`[FamilyMember] New member added to family: ${familyId}`);

    try {
      // Get all family members
      const membersSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .get();

      console.log(`[FamilyMember] Notifying ${membersSnapshot.size} family members`);

      // Notify all other members
      for (const memberDoc of membersSnapshot.docs) {
        if (memberDoc.id === userId) {
          continue; // Don't notify the new member
        }

        const memberData = memberDoc.data();
        const fcmToken = memberData.fcmToken;

        if (!fcmToken) {
          console.log(`[FamilyMember] Member has no FCM token: ${memberDoc.id}`);
          continue;
        }

        try {
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
            android: { priority: "high" },
          });

          console.log(`[FamilyMember] Notification sent to: ${memberDoc.id}`);
        } catch (error: any) {
          if (error?.code === "messaging/registration-token-not-registered") {
            console.log(`[FamilyMember] Invalid token for ${memberDoc.id}, clearing...`);
            await db.collection("users").doc(memberDoc.id).update({ fcmToken: "" });
          } else {
            console.error(`[FamilyMember] Error sending to ${memberDoc.id}:`, error);
          }
        }
      }
    } catch (error) {
      console.error("[FamilyMember] Error:", error);
    }
  });