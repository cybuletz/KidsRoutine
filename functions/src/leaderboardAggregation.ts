import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

/**
 * aggregateLeaderboards
 * ─────────────────────
 * Scheduled Cloud Function — runs every day at 02:00 UTC.
 * Reads live Firestore data → writes pre-computed snapshots into:
 *
 *   leaderboard_snapshots/children   { entries: [...], computedAt }
 *   leaderboard_snapshots/families   { entries: [...], computedAt }
 *   leaderboard_snapshots/challenges { entries: [...], computedAt }
 *
 * The Android app reads ONLY from leaderboard_snapshots — zero heavy queries.
 */
export const aggregateLeaderboards = functions.pubsub
  .schedule("0 2 * * *")          // every day at 02:00 UTC
  .timeZone("UTC")
  .onRun(async (_context) => {
    const db        = admin.firestore();
    const now       = admin.firestore.Timestamp.now();
    const batchSize = 100;

    console.log("[Leaderboard] Starting nightly aggregation...");

    // ── 1. CHILDREN leaderboard (top 100 by XP) ──────────────────────────
    try {
      const childSnap = await db.collection("users")
        .orderBy("xp", "asc")           // fetch ascending, sort in memory below
        .limit(batchSize)
        .get();

      // Reverse so highest XP = rank 1
      const childEntries = childSnap.docs
        .map((doc) => ({
          userId:      doc.id,
          displayName: doc.get("displayName") as string || "Unknown",
          familyId:    doc.get("familyId")    as string || "",
          avatarUrl:   doc.get("avatarUrl")   as string || "",
          xp:          doc.get("xp")          as number || 0,
          level:       doc.get("level")       as number || 1,
          streak:      doc.get("streak")      as number || 0,
          badges:      (doc.get("badges") as any[])?.length || 0,
          rank:        0,                     // filled in below
        }))
        .sort((a, b) => b.xp - a.xp)
        .map((e, idx) => ({ ...e, rank: idx + 1 }));

      await db.collection("leaderboard_snapshots").doc("children").set({
        entries:    childEntries,
        computedAt: now,
      });
      console.log(`[Leaderboard] ✅ Children snapshot written (${childEntries.length} entries)`);
    } catch (err: any) {
      console.error("[Leaderboard] ❌ Children error:", err.message);
    }

    // ── 2. FAMILIES leaderboard (top 100 by familyStreak) ────────────────
    try {
      const familySnap = await db.collection("families")
        .orderBy("familyStreak", "asc")
        .limit(batchSize)
        .get();

      const familyEntries = familySnap.docs
        .map((doc) => ({
          familyId:    doc.id,
          familyName:  doc.get("familyName")  as string || "Unknown Family",
          streak:      doc.get("familyStreak") as number || 0,
          familyXp:    doc.get("familyXp")     as number || 0,
          memberCount: (doc.get("memberIds") as any[])?.length || 0,
          rank:        0,
        }))
        .sort((a, b) => b.streak - a.streak)
        .map((e, idx) => ({ ...e, rank: idx + 1 }));

      await db.collection("leaderboard_snapshots").doc("families").set({
        entries:    familyEntries,
        computedAt: now,
      });
      console.log(`[Leaderboard] ✅ Families snapshot written (${familyEntries.length} entries)`);
    } catch (err: any) {
      console.error("[Leaderboard] ❌ Families error:", err.message);
    }

    // ── 3. CHALLENGES leaderboard (top 50 by completion count) ───────────
    try {
      // FIX: .where() instead of .whereEqualTo() for CollectionGroup
      const progressSnap = await db.collectionGroup("challenge_progress")
        .where("status", "==", "COMPLETED")
        .limit(500)
        .get();

      // Count completions per challenge
      const counts: Record<string, number> = {};
      for (const doc of progressSnap.docs) {
        const cid = doc.get("challengeId") as string;
        if (cid) counts[cid] = (counts[cid] || 0) + 1;
      }

      // Fetch challenge titles for top 50
      const sorted = Object.entries(counts)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 50);

      const challengeEntries = await Promise.all(
        sorted.map(async ([challengeId, completions], idx) => {
          let title = challengeId;
          try {
            const cdoc = await db.collection("challenges").doc(challengeId).get();
            title = cdoc.get("title") as string || challengeId;
          } catch (_) {}
          return { rank: idx + 1, challengeId, title, completions };
        })
      );

      await db.collection("leaderboard_snapshots").doc("challenges").set({
        entries:    challengeEntries,
        computedAt: now,
      });
      console.log(`[Leaderboard] ✅ Challenges snapshot written (${challengeEntries.length} entries)`);
    } catch (err: any) {
      console.error("[Leaderboard] ❌ Challenges error:", err.message);
    }

    console.log("[Leaderboard] ✅ Nightly aggregation complete.");
    return null;
  });