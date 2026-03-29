import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Challenge Assignment Notifications
 * Listens to challengeAssignments creation
 * Notifies child of new challenge
 */
export const notifyChallengeAssignment = functions.firestore
  .document("challengeAssignments/{docId}")
  .onCreate(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const challengeId = assignment.challengeId;
    const familyId = assignment.familyId;

    console.log(`[ChallengeAssignment] Challenge assigned to child: ${childId}`);

    try {
      // Get challenge details
      const challengeDoc = await db.collection("challenges").doc(challengeId).get();
      if (!challengeDoc.exists) {
        console.log(`[ChallengeAssignment] Challenge not found: ${challengeId}`);
        return;
      }

      const challenge = challengeDoc.data();
      if (!challenge) {
        console.log(`[ChallengeAssignment] Challenge data is empty: ${challengeId}`);
        return;
      }

      // Get child's FCM token
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[ChallengeAssignment] Child not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[ChallengeAssignment] No FCM token for child: ${childId}`);
        return;
      }

      // Send notification to child
      await messaging.send({
        token: fcmToken,
        notification: {
          title: `🏆 New Challenge: ${challenge.title}`,
          body: challenge.description || "A new challenge has been assigned to you",
        },
        data: {
          type: "CHALLENGE_ASSIGNED",
          userId: childId,
          challengeId: challengeId,
          refreshTrigger: "true"
        },
        android: { priority: "high" },
      });

      console.log(`[ChallengeAssignment] Notification sent to child: ${childId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        console.log(`[ChallengeAssignment] Invalid token for child: ${childId}`);
        await db.collection("users").doc(childId).update({ fcmToken: "" });
      } else {
        console.error(`[ChallengeAssignment] Error:`, error);
      }
    }
  });