import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// ✅ INITIALIZE FIREBASE FIRST
admin.initializeApp();

// ✅ Import AI functions
import * as aiGeneration from "./aiGeneration";
import * as storyGeneration from "./storyGeneration";
import * as leaderboardAggregation from "./leaderboardAggregation";

// ✅ Import notifications
import * as notifications from "./notifications";

// ✅ Import leaderboard
import * as leaderboard from "./leaderboard";

// ✅ Import content packs
import * as contentPacks from "./contentPacks";

// ===== EXPORT AI FUNCTIONS =====
export const generateTasksAI = aiGeneration.generateTasksAI;
export const generateChallengesAI = aiGeneration.generateChallengesAI;
export const generateDailyPlanAI = aiGeneration.generateDailyPlanAI;
export const generateWeeklyPlanAI = aiGeneration.generateWeeklyPlanAI;
export const generateStoryTaskAI = storyGeneration.generateStoryTaskAI;

// ===== EXPORT LEADERBOARD FUNCTIONS =====
export { aggregateLeaderboards, computeLeaderboardsManual } from "./leaderboardAggregation";
export const computeLeaderboardSnapshots = leaderboard.computeLeaderboardSnapshots;

// ===== EXPORT NOTIFICATION FUNCTIONS =====
// Task Completion
export const notifyTaskCompletion = notifications.notifyTaskCompletion;
export const notifyTaskCompletionFamilyScoped = notifications.notifyTaskCompletionFamilyScoped;

// Task Management
export const notifyTaskDeletion = notifications.notifyTaskDeletion;
export const notifyTaskUpdate = notifications.notifyTaskUpdate;
export const notifyTaskInstanceUpdateFamilyScoped = notifications.notifyTaskInstanceUpdateFamilyScoped;
export const notifyTaskAssignment = notifications.notifyTaskAssignment;

// Approval & Feedback
export const notifyTaskApproval = notifications.notifyTaskApproval;
export const notifyAchievementUnlock = notifications.notifyAchievementUnlock;

// Social
export const notifyFamilyChat = notifications.notifyFamilyChat;
export const notifyFamilyMemberAdded = notifications.notifyFamilyMemberAdded;

// Challenges
export const notifyChallengeAssignment = notifications.notifyChallengeAssignment;

// Parent Controls & XP Bank
export const notifyXpLoanCreated = notifications.notifyXpLoanCreated;
export const notifyXpLoanUpdate = notifications.notifyXpLoanUpdate;
export const notifyParentControlUpdate = notifications.notifyParentControlUpdate;

// ===== EXPORT CONTENT PACK FUNCTIONS =====
export const applyContentPack = contentPacks.applyContentPack;

// ===== EXPORT MIGRATION FUNCTIONS =====
import * as migration from "./migration";
export const cleanupLegacyData = migration.cleanupLegacyData;
export const deleteAllData = migration.deleteAllData;
