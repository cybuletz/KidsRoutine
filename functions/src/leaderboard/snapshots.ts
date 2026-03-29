import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();

/**
 * Leaderboard Snapshot Computation
 * Runs every 6 hours
 * Pre-computes global leaderboard snapshots for fast queries
 */
export const computeLeaderboardSnapshots = functions.pubsub
  .schedule("every 6 hours")
  .onRun(async (_context) => {
    console.log("[Leaderboard] Computing snapshots...");

    try {
      // ── Children Leaderboard ──────────────────────────────────────
      const childSnap = await db.collection("users")
        .orderBy("xp", "desc")
        .limit(100)
        .get();

      const childEntries = childSnap.docs.map((doc, index) => ({
        rank:        index + 1,
        userId:      doc.id,
        displayName: doc.data().displayName ?? "Unknown",
        familyId:    doc.data().familyId    ?? "",
        avatarUrl:   doc.data().avatarUrl   ?? "",
        xp:          doc.data().xp          ?? 0,
        level:       doc.data().level       ?? 1,
        streak:      doc.data().streak      ?? 0,
        badges:      (doc.data().badges as any[])?.length ?? 0,
      }));

      await db.collection("leaderboard_snapshots").doc("children").set({
        entries:    childEntries,
        computedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`[Leaderboard] ✅ Children snapshot: ${childEntries.length} entries`);

      // ── Families Leaderboard ──────────────────────────────────────
      const familySnap = await db.collection("families")
        .orderBy("familyStreak", "desc")
        .limit(50)
        .get();

      const familyEntries = familySnap.docs.map((doc, index) => ({
        rank:        index + 1,
        familyId:    doc.id,
        familyName:  doc.data().familyName  ?? "Unknown Family",
        streak:      doc.data().familyStreak ?? 0,
        familyXp:    doc.data().familyXp    ?? 0,
        memberCount: (doc.data().memberIds as any[])?.length ?? 0,
      }));

      await db.collection("leaderboard_snapshots").doc("families").set({
        entries:    familyEntries,
        computedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`[Leaderboard] ✅ Families snapshot: ${familyEntries.length} entries`);

      // ── Challenges Leaderboard ────────────────────────────────────
      const progressSnap = await db.collectionGroup("challenge_progress")
        .where("status", "==", "COMPLETED")
        .get();

      const challengeStats: Record<string, { count: number; days: number[] }> = {};

      progressSnap.docs.forEach((doc: admin.firestore.QueryDocumentSnapshot) => {
        const cid = doc.data().challengeId;
        if (!cid) return;
        if (!challengeStats[cid]) challengeStats[cid] = { count: 0, days: [] };
        challengeStats[cid].count++;
        if (doc.data().totalDays) challengeStats[cid].days.push(doc.data().totalDays);
      });

      const challengeEntries = await Promise.all(
        Object.entries(challengeStats)
          .sort(([, a], [, b]) => b.count - a.count)
          .slice(0, 50)
          .map(async ([challengeId, stats], index) => {
            let title = "Unknown Challenge";
            try {
              const cdoc = await db.collection("challenges").doc(challengeId).get();
              if (cdoc.exists) title = cdoc.data()?.title ?? title;
            } catch (_) {}
            return {
              rank:                 index + 1,
              challengeId,
              title,
              completions:          stats.count,
              averageCompletionDays: stats.days.length
                ? stats.days.reduce((a, b) => a + b, 0) / stats.days.length
                : 0,
            };
          })
      );

      await db.collection("leaderboard_snapshots").doc("challenges").set({
        entries:    challengeEntries,
        computedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`[Leaderboard] ✅ Challenges snapshot: ${challengeEntries.length} entries`);
      console.log(
        `[Leaderboard] ✅ All snapshots computed — ` +
        `${childEntries.length} children, ${familyEntries.length} families, ` +
        `${challengeEntries.length} challenges`
      );

    } catch (error) {
      console.error("[Leaderboard] ❌ Error computing snapshots:", error);
    }
  });