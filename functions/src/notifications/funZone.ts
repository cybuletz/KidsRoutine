import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

const DEFAULT_STAT = 100;
const HAPPINESS_LOW_THRESHOLD = 40;
const ENERGY_LOW_THRESHOLD = 30;
const NEGLECT_THRESHOLD = 20;

/**
 * Pet Feeding Reminder — triggered when pet stats are updated.
 * If happiness or energy drops below threshold, sends a nudge to the child.
 *
 * Firestore path: pets/{petId}
 */
export const notifyPetNeedsAttention = functions.firestore
  .document("pets/{petId}")
  .onUpdate(async (change: any, context: any) => {
    const after = change.after.data();
    const before = change.before.data();

    if (!after || !after.userId) {
      console.log("[PetNotify] Missing pet data or userId");
      return;
    }

    const userId = after.userId;
    const happiness = after.happiness ?? DEFAULT_STAT;
    const energy = after.energy ?? DEFAULT_STAT;
    const prevHappiness = before?.happiness ?? DEFAULT_STAT;
    const prevEnergy = before?.energy ?? DEFAULT_STAT;
    const petName = after.name || "Your pet";

    // Only notify when stats drop BELOW thresholds (not on every update)
    const happinessDropped = prevHappiness >= HAPPINESS_LOW_THRESHOLD && happiness < HAPPINESS_LOW_THRESHOLD;
    const energyDropped = prevEnergy >= ENERGY_LOW_THRESHOLD && energy < ENERGY_LOW_THRESHOLD;
    const isNeglected = happiness < NEGLECT_THRESHOLD && energy < NEGLECT_THRESHOLD;

    if (!happinessDropped && !energyDropped && !isNeglected) {
      return; // No notification needed
    }

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        console.log(`[PetNotify] User not found: ${userId}`);
        return;
      }

      const fcmToken = userDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[PetNotify] No FCM token for user: ${userId}`);
        return;
      }

      let title: string;
      let body: string;

      if (isNeglected) {
        title = `😿 ${petName} misses you!`;
        body = `${petName} is feeling sad and tired. Complete a task to cheer them up!`;
      } else if (happinessDropped) {
        title = `🐾 ${petName} is getting lonely`;
        body = `${petName}'s happiness is low. Feed or play with them!`;
      } else {
        title = `⚡ ${petName} needs energy`;
        body = `${petName} is running low on energy. Time for a snack!`;
      }

      await messaging.send({
        token: fcmToken,
        notification: { title, body },
        data: {
          type: "PET_NEEDS_ATTENTION",
          userId: userId,
          petId: context.params.petId,
          happiness: String(happiness),
          energy: String(energy),
        },
        android: { priority: "normal" },
      });
      console.log(`[PetNotify] Sent pet attention notification to ${userId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        await db.collection("users").doc(userId).update({ fcmToken: "" });
        console.log(`[PetNotify] Cleared stale FCM token for user: ${userId}`);
      }
      console.error(`[PetNotify] Error: ${error.message}`);
    }
  });

/**
 * Daily Spin Available — triggered when a new daily spin state is created.
 * Notifies the child that they have a spin available today.
 *
 * Firestore path: daily_spins/{stateId}
 */
export const notifyDailySpinAvailable = functions.firestore
  .document("daily_spins/{stateId}")
  .onCreate(async (snap: any, context: any) => {
    const data = snap.data();

    if (!data || !data.userId) {
      console.log("[SpinNotify] Missing spin data or userId");
      return;
    }

    const userId = data.userId;
    const spinsRemaining = (data.maxSpins ?? 1) - (data.spinsUsed ?? 0);

    if (spinsRemaining <= 0) {
      console.log("[SpinNotify] No spins remaining, skipping notification");
      return;
    }

    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        console.log(`[SpinNotify] User not found: ${userId}`);
        return;
      }

      const fcmToken = userDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[SpinNotify] No FCM token for user: ${userId}`);
        return;
      }

      await messaging.send({
        token: fcmToken,
        notification: {
          title: "🎡 Daily Spin Ready!",
          body: `You have ${spinsRemaining} spin${spinsRemaining > 1 ? "s" : ""} available today! Try your luck!`,
        },
        data: {
          type: "DAILY_SPIN_AVAILABLE",
          userId: userId,
          spinsRemaining: String(spinsRemaining),
        },
        android: { priority: "normal" },
      });
      console.log(`[SpinNotify] Sent daily spin notification to ${userId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        await db.collection("users").doc(userId).update({ fcmToken: "" });
        console.log(`[SpinNotify] Cleared stale FCM token for user: ${userId}`);
      }
      console.error(`[SpinNotify] Error: ${error.message}`);
    }
  });
