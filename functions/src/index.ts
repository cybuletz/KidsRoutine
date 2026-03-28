import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// ✅ INITIALIZE FIREBASE FIRST - BEFORE ANY IMPORTS THAT USE IT
admin.initializeApp();

// ✅ NOW import modules that need Firebase
import * as aiGeneration from "./aiGeneration";
import * as storyGeneration from "./storyGeneration";
import * as leaderboardAggregation from "./leaderboardAggregation";
//import * as firebaseSetup from "./setupFirestore";

const db = admin.firestore();
const messaging = admin.messaging();

// ===== Export AI functions =====
//export const initializeFirestore = firebaseSetup.setupFirestore;
//export const getFirestoreRules = firebaseSetup.setupFirestoreRules;
export const generateTasksAI = aiGeneration.generateTasksAI;
export const generateChallengesAI = aiGeneration.generateChallengesAI;
export const generateDailyPlanAI = aiGeneration.generateDailyPlanAI;
export const generateWeeklyPlanAI = aiGeneration.generateWeeklyPlanAI;
export const generateStoryTaskAI    = storyGeneration.generateStoryTaskAI;


// ===== Leaderboard aggregation =====
export const aggregateLeaderboards  = leaderboardAggregation.aggregateLeaderboards;

// ===== TASK COMPLETION NOTIFICATIONS =====

export const notifyTaskCompletion = functions.firestore
  .document("taskProgress/{taskId}")
  .onCreate(async (snap: any, context: any) => {
    const newData = snap.data();

    if (!newData || !newData.userId || !newData.status) {
      console.log("[Task Completion] Missing required fields");
      return;
    }

    if (newData.status !== "COMPLETED") {
      console.log("[Task Completion] Task not completed, status:", newData.status);
      return;
    }

    const userId = newData.userId;
    const taskTitle = newData.taskTitle || "Task";
    const xpGained = newData.xpGained || 0;
    const validationStatus = newData.validationStatus || "";
    const taskInstanceId = newData.taskInstanceId || "";

    console.log(`[Task Completion] Task completed by user: ${userId}`);
    console.log(`[Task Completion] Task ID: ${taskInstanceId}`);
    console.log(`[Task Completion] Validation status: ${validationStatus}`);

    try {
      // Get user document to find family and FCM token
      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        console.log(`[Task Completion] User document not found: ${userId}`);
        return;
      }

      const userData = userDoc.data();
      const userFcmToken = userData?.fcmToken;
      const familyId = userData?.familyId;
      const childDisplayName = userData?.displayName || "Your child";

      console.log(`[Task Completion] User FCM token exists: ${!!userFcmToken}`);
      console.log(`[Task Completion] Family ID: ${familyId}`);

      if (!userFcmToken) {
        console.log(`[Task Completion] No FCM token for user: ${userId}`);
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
        console.log(`[Task Completion] Notification sent to child: ${userId}`);
      } catch (error: any) {
        if (error?.code === "messaging/registration-token-not-registered") {
          console.log(`[Task Completion] Child token invalid, deleting: ${userId}`);
          await db.collection("users").doc(userId).update({ fcmToken: "" });
        } else {
          console.error(`[Task Completion] Error sending to child:`, error);
        }
        return;
      }

      // Always notify parents (regardless of validation status)
      if (!familyId) {
        console.log(`[Task Completion] No family ID for user: ${userId}`);
        return;
      }

      console.log(`[Task Completion] Looking for parents in family: ${familyId}`);

      const parentsSnapshot = await db
        .collection("users")
        .where("familyId", "==", familyId)
        .where("role", "==", "PARENT")
        .get();

      console.log(`[Task Completion] Found ${parentsSnapshot.size} parents`);

      for (const parentDoc of parentsSnapshot.docs) {
        const parentData = parentDoc.data();
        const parentFcmToken = parentData.fcmToken;

        if (!parentFcmToken) {
          console.log(`[Task Completion] Parent has no FCM token: ${parentDoc.id}`);
          continue;
        }

        try {
          const title = validationStatus === "PENDING"
            ? "Task Needs Approval ⏳"
            : "Task Completed ✅";

          const body = validationStatus === "PENDING"
            ? `${childDisplayName} completed: ${taskTitle}`
            : `${childDisplayName} completed: ${taskTitle} (Auto-approved)`;

          await messaging.send({
            token: parentFcmToken,
            notification: {
              title: title,
              body: body,
            },
            data: {
              type: validationStatus === "PENDING" ? "PARENT_APPROVAL_NEEDED" : "TASK_COMPLETION",
              childId: userId,
              taskTitle: taskTitle,
              status: validationStatus,
            },
            android: {
              priority: "high",
            },
          });

          console.log(`[Task Completion] Parent notification sent: ${parentDoc.id}`);
        } catch (parentError: any) {
          if (parentError?.code === "messaging/registration-token-not-registered") {
            console.log(`[Task Completion] Parent token invalid, deleting: ${parentDoc.id}`);
            await db.collection("users").doc(parentDoc.id).update({ fcmToken: "" });
          } else {
            console.error(`[Task Completion] Error sending to parent:`, parentError);
          }
        }
      }

      // Track task completion count for achievements
      await db.collection("users").doc(userId).update({
        tasksCompleted: admin.firestore.FieldValue.increment(1)
      });
      console.log(`[Task Completion] Incremented tasksCompleted for user: ${userId}`);

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
            continue;
          }

          const memberData = memberDoc.data();
          const fcmToken = memberData.fcmToken;

          if (!fcmToken) {
            console.log(`[Family Chat] Member has no FCM token: ${memberDoc.id}`);
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

            console.log(`[Family Chat] Message notification sent to: ${memberDoc.id}`);
          } catch (error: any) {
            if (error?.code === "messaging/registration-token-not-registered") {
              console.log(`[Family Chat] Invalid token for ${memberDoc.id}, deleting...`);
              await db.collection("users").doc(memberDoc.id).update({ fcmToken: "" });
            } else {
              console.error(`[Family Chat] Error sending to ${memberDoc.id}:`, error);
            }
          }
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

// ===== TASK ASSIGNMENT NOTIFICATIONS =====

export const notifyTaskAssignment = functions.firestore
  .document("taskAssignments/{docId}")
  .onCreate(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const taskId = assignment.taskId;
    const familyId = assignment.familyId;

    console.log(`[Task Assignment] Task assigned to child: ${childId}`);

    try {
      // Get task details
      const taskDoc = await db.collection("tasks").doc(taskId).get();
      if (!taskDoc.exists) {
        console.log(`[Task Assignment] Task not found: ${taskId}`);
        return;
      }
      const task = taskDoc.data();

      // ✅ ADD THIS CHECK
      if (!task) {
        console.log(`[Task Assignment] Task data is empty: ${taskId}`);
        return;
      }

      // Get child's FCM token
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[Task Assignment] Child not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[Task Assignment] No FCM token for child: ${childId}`);
        return;
      }

      // Send notification to child
      await messaging.send({
        token: fcmToken,
        notification: {
          title: `📋 New Task: ${task.title}`,
          body: task.description || "A new task has been assigned to you",
        },
        data: {
          type: "TASK_ASSIGNED",
          userId: childId,
          taskId: taskId,
          icon: "✨",
          refreshTrigger: "true"
        },
        android: {
          priority: "high",
        },
      });

      console.log(`[Task Assignment] Notification sent to child: ${childId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        console.log(`[Task Assignment] Invalid token for child: ${childId}`);
        await db.collection("users").doc(childId).update({ fcmToken: "" });
      } else {
        console.error(`[Task Assignment] Error:`, error);
      }
    }
  });

// ===== CHALLENGE ASSIGNMENT NOTIFICATIONS =====

export const notifyChallengeAssignment = functions.firestore
  .document("challengeAssignments/{docId}")
  .onCreate(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const challengeId = assignment.challengeId;
    const familyId = assignment.familyId;

    console.log(`[Challenge Assignment] Challenge assigned to child: ${childId}`);

    try {
      // Get challenge details
      const challengeDoc = await db.collection("challenges").doc(challengeId).get();
      if (!challengeDoc.exists) {
        console.log(`[Challenge Assignment] Challenge not found: ${challengeId}`);
        return;
      }
      const challenge = challengeDoc.data();

      // ✅ ADD THIS CHECK
      if (!challenge) {
        console.log(`[Challenge Assignment] Challenge data is empty: ${challengeId}`);
        return;
      }

      // Get child's FCM token
      const childDoc = await db.collection("users").doc(childId).get();
      if (!childDoc.exists) {
        console.log(`[Challenge Assignment] Child not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`[Challenge Assignment] No FCM token for child: ${childId}`);
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
          icon: "🎯",
          refreshTrigger: "true"
        },
        android: {
          priority: "high",
        },
      });

      console.log(`[Challenge Assignment] Notification sent to child: ${childId}`);
    } catch (error: any) {
      if (error?.code === "messaging/registration-token-not-registered") {
        console.log(`[Challenge Assignment] Invalid token for child: ${childId}`);
        await db.collection("users").doc(childId).update({ fcmToken: "" });
      } else {
        console.error(`[Challenge Assignment] Error:`, error);
      }
    }
  });

// ===== SCHEDULED LEADERBOARD SNAPSHOT COMPUTATION =====
// Runs every 6 hours — pre-computes global leaderboard snapshots

export const computeLeaderboardSnapshots = functions.pubsub
  .schedule("every 6 hours")
  .onRun(async (_context) => {
    console.log("[Leaderboard] Computing snapshots...");

    try {
      // ── Children ──────────────────────────────────────────────────────
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

      // ── Families ──────────────────────────────────────────────────────
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

      // ── Challenges ───────────────────────────────────────────────────
            const progressSnap = await db.collectionGroup("challenge_progress")
              .where("status", "==", "COMPLETED")   // ← was .whereEqualTo(...)
              .get();

            const challengeStats: Record<string, { count: number; days: number[] }> = {};
            progressSnap.docs.forEach((doc: admin.firestore.QueryDocumentSnapshot) => {  // ← explicit type fixes TS7006
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
                    // FIX: regular collection lookup — collectionGroup + whereEqualTo is invalid here
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

      console.log(
        `[Leaderboard] ✅ Snapshots written — ` +
        `${childEntries.length} children, ${familyEntries.length} families, ` +
        `${challengeEntries.length} challenges`
      );
    } catch (error) {
      console.error("[Leaderboard] ❌ Error computing snapshots:", error);
    }
  });


    // Seed content pack tasks into family task pool after unlock
export const applyContentPack = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Must be signed in.");
    }

    const { userId, familyId, packId } = data as {
        userId: string;
        familyId: string;
        packId: string;
    };

    if (!userId || !familyId || !packId) {
        throw new functions.https.HttpsError("invalid-argument", "userId, familyId, packId required.");
    }

    const db = admin.firestore();

    // 1. Load the content pack's task definitions from Firestore
    const packDoc = await db.collection("content_packs").doc(packId).get();
    if (!packDoc.exists) {
        throw new functions.https.HttpsError("not-found", `Content pack '${packId}' not found in Firestore.`);
    }

    const packData = packDoc.data()!;
    const taskTemplates: any[] = packData.tasks || [];

    if (taskTemplates.length === 0) {
        console.log(`Pack ${packId} has no tasks to seed.`);
        return { seeded: 0 };
    }

    // 2. Write each task into families/{familyId}/tasks
    const batch = db.batch();
    const familyTasksRef = db.collection("families").doc(familyId).collection("tasks");

    taskTemplates.forEach((template: any) => {
        const taskRef = familyTasksRef.doc(); // auto-ID
        batch.set(taskRef, {
            ...template,
            taskId:    taskRef.id,
            familyId:  familyId,
            createdBy: userId,
            source:    "content_pack",
            packId:    packId,
            isActive:  true,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
    });

    await batch.commit();
    console.log(`✓ Seeded ${taskTemplates.length} tasks from pack '${packId}' into family '${familyId}'`);
    return { seeded: taskTemplates.length };
});

// ===== TASK DELETION NOTIFICATIONS =====

export const notifyTaskDeletion = functions.firestore
  .document("taskAssignments/{docId}")
  .onDelete(async (snap: any, context: any) => {
    const assignment = snap.data();
    const childId = assignment.childId;
    const taskId = assignment.taskId;
    const familyId = assignment.familyId;

    console.log(`[Task Deletion] 🗑�� Assignment deleted for child: ${childId}, taskId: ${taskId}`);

    if (!childId || !taskId) {
      console.warn(`[Task Deletion] ⚠️ Missing childId or taskId. childId=${childId}, taskId=${taskId}`);
      return;
    }

    try {
      // Get task details (may already be deleted, so this is optional)
      let taskTitle = "Task";
      try {
        const taskDoc = await db.collection("tasks").doc(taskId).get();
        if (taskDoc.exists) {
          taskTitle = taskDoc.data()?.title || "Task";
          console.log(`[Task Deletion] Found task title: ${taskTitle}`);
        } else {
          console.log(`[Task Deletion] Task doc not found in global collection: ${taskId}`);
        }
      } catch (e: any) {
        console.log(`[Task Deletion] Could not fetch task details: ${e.message}`);
      }

      // Get child's FCM token
      console.log(`[Task Deletion] Looking up child doc: ${childId}`);
      const childDoc = await db.collection("users").doc(childId).get();

      if (!childDoc.exists) {
        console.warn(`[Task Deletion] ⚠️ Child user not found: ${childId}`);
        return;
      }

      const fcmToken = childDoc.data()?.fcmToken;
      console.log(`[Task Deletion] Child FCM token exists: ${!!fcmToken}`);

      if (!fcmToken) {
        console.log(`[Task Deletion] ⚠️ No FCM token for child: ${childId} (child hasn't opened app yet?)`);
        return;
      }

      // Send notification to child
      try {
        console.log(`[Task Deletion] 📤 Sending FCM notification to token: ${fcmToken.substring(0, 20)}...`);

        const messageId = await messaging.send({
          token: fcmToken,
          notification: {
            title: `📋 Task Removed`,
            body: `"${taskTitle}" has been deleted`,
          },
          data: {
            type: "TASK_DELETED",
            userId: childId,
            taskId: taskId,
            icon: "🗑️",
            refreshTrigger: "true"  // ← This triggers child UI refresh
          },
          android: {
            priority: "high",
          },
        });

        console.log(`[Task Deletion] ✅ FCM sent successfully! Message ID: ${messageId}`);

      } catch (error: any) {
        if (error?.code === "messaging/registration-token-not-registered") {
          console.log(`[Task Deletion] 🔄 Invalid token for child: ${childId}, clearing from DB...`);
          await db.collection("users").doc(childId).update({ fcmToken: "" });
        } else {
          console.error(`[Task Deletion] ❌ FCM error: ${error.code} - ${error.message}`);
        }
      }

    } catch (error: any) {
      console.error(`[Task Deletion] ❌ Unexpected error: ${error.message}`);
    }
  });