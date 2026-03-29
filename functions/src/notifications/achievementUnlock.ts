import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Achievement Unlock Notifications
 * Listens to users collection updates
 * Notifies child when new badge is unlocked
 */
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
        android: { priority: "high" },
      });

      console.log(`[Achievement] Notification sent to user: ${userId}`);
    } catch (error) {
      console.error("[Achievement] Error:", error);
    }
  });