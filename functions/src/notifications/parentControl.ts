import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Parent Control Update Notification — triggered when parent changes controls.
 * Sends a refresh trigger to the child so their app reloads Fun Zone settings.
 * Also logs the change for audit purposes.
 *
 * Firestore path: families/{familyId}/parent_controls/{childId}
 */
export const notifyParentControlUpdate = functions.firestore
  .document("families/{familyId}/parent_controls/{childId}")
  .onWrite(async (change: any, context: any) => {
    const { familyId, childId } = context.params;
    const after = change.after.exists ? change.after.data() : null;
    const before = change.before.exists ? change.before.data() : null;

    if (!after) {
      console.log(`[ParentControls] Controls deleted for child: ${childId}`);
      return;
    }

    // If this is a create (no before), or an update (before exists)
    const isCreate = !before;
    console.log(`[ParentControls] Controls ${isCreate ? "created" : "updated"} for child: ${childId} in family: ${familyId}`);

    try {
      // ── Send refresh trigger to child's device ──────────────────────
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[ParentControls] Child not found: ${childId}`);
        return;
      }

      const childFcm = childDoc.data()?.fcmToken;
      if (!childFcm) {
        console.log(`[ParentControls] No FCM token for child: ${childId}`);
        return;
      }

      // Build a summary of what changed
      const changes: string[] = [];
      if (before) {
        const funZoneKeys = [
          "petEnabled", "bossBattleEnabled", "dailySpinEnabled",
          "storyArcsEnabled", "eventsEnabled", "skillTreeEnabled",
          "walletEnabled", "ritualsEnabled"
        ];
        for (const key of funZoneKeys) {
          if (before[key] !== after[key]) {
            const featureName = key.replace("Enabled", "").replace(/([A-Z])/g, " $1").trim();
            changes.push(`${featureName}: ${after[key] ? "✅ On" : "❌ Off"}`);
          }
        }
        if (before.defaultDifficulty !== after.defaultDifficulty) {
          changes.push(`Default difficulty: ${after.defaultDifficulty}`);
        }
      }

      // Send a silent data message to trigger screen refresh
      await messaging.send({
        token: childFcm,
        data: {
          type: "PARENT_CONTROLS_UPDATED",
          userId: childId,
          familyId: familyId,
          refreshTrigger: "true",
          changeCount: String(changes.length),
        },
        android: { priority: "high" },
      });
      console.log(`[ParentControls] Refresh trigger sent to child: ${childId}`);

      // ── If significant changes, send a visible notification too ─────
      if (changes.length > 0 && !isCreate) {
        try {
          await messaging.send({
            token: childFcm,
            notification: {
              title: "⚙️ Fun Zone Updated",
              body: changes.length <= 2
                ? changes.join(", ")
                : `${changes.length} settings were updated by your parent`,
            },
            data: {
              type: "PARENT_CONTROLS_CHANGED",
              userId: childId,
              familyId: familyId,
              refreshTrigger: "true",
            },
            android: { priority: "normal" },
          });
          console.log(`[ParentControls] Change notification sent to child: ${childId}`);
        } catch (notifError: any) {
          if (notifError?.code === "messaging/registration-token-not-registered") {
            await db.collection("users").doc(childId).update({ fcmToken: "" });
          }
        }
      }

      // ── Send silent refresh trigger to parents (they initiated the action,
      //    so no visible notification needed — just a data message for screen sync) ─
      if (!isCreate && changes.length > 0) {
        try {
          const parentSnapshot = await db
            .collection("users")
            .where("familyId", "==", familyId)
            .where("role", "==", "PARENT")
            .get();

          for (const parentDoc of parentSnapshot.docs) {
            const parentFcm = parentDoc.data()?.fcmToken;
            if (!parentFcm) continue;

            const childName = childDoc.data()?.displayName || "Child";

            await messaging.send({
              token: parentFcm,
              data: {
                type: "PARENT_CONTROLS_SAVED",
                userId: parentDoc.id,
                familyId: familyId,
                childId: childId,
                refreshTrigger: "true",
              },
              android: { priority: "normal" },
            });
          }
          console.log(`[ParentControls] Parent confirmation sent`);
        } catch (parentError: any) {
          console.warn(`[ParentControls] Could not notify parents: ${parentError.message}`);
        }
      }
    } catch (error: any) {
      console.error(`[ParentControls] Error: ${error.message}`);
    }
  });
