import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * XP Loan Notification — triggered when a new loan is created.
 * Notifies the child that they received XP from their parent.
 * Also sends a confirmation notification to the parent.
 *
 * Firestore path: families/{familyId}/xp_loans/{loanId}
 */
export const notifyXpLoanCreated = functions.firestore
  .document("families/{familyId}/xp_loans/{loanId}")
  .onCreate(async (snap: any, context: any) => {
    const loan = snap.data();
    const { familyId, loanId } = context.params;

    if (!loan || !loan.childId || !loan.parentId) {
      console.log("[XpLoan] Missing required fields");
      return;
    }

    const childId = loan.childId;
    const parentId = loan.parentId;
    const amount = loan.amount || 0;
    const childName = loan.childName || "Child";
    const note = loan.note || "";
    const repayPct = loan.repaymentPercentage || 20;

    console.log(`[XpLoan] New loan created: ${amount} XP to ${childName} (${childId}) in family ${familyId}`);

    try {
      // ── Notify the child ────────────────────────────────────────────
      const childDoc = await db.collection("users").doc(childId).get();
      if (childDoc.exists) {
        const childFcm = childDoc.data()?.fcmToken;
        if (childFcm) {
          try {
            const bodyParts = [`You received ${amount} XP from your parent!`];
            if (note) bodyParts.push(`📝 "${note}"`);
            bodyParts.push(`${repayPct}% of future task XP will go toward repayment.`);

            await messaging.send({
              token: childFcm,
              notification: {
                title: "🏦 XP Loan Received!",
                body: bodyParts.join(" "),
              },
              data: {
                type: "XP_LOAN_RECEIVED",
                userId: childId,
                familyId: familyId,
                loanId: loanId,
                amount: String(amount),
                refreshTrigger: "true",
              },
              android: { priority: "high" },
            });
            console.log(`[XpLoan] Notification sent to child: ${childId}`);
          } catch (error: any) {
            if (error?.code === "messaging/registration-token-not-registered") {
              await db.collection("users").doc(childId).update({ fcmToken: "" });
            }
          }
        }
      }

      // ── Confirm to the parent ───────────────────────────────────────
      const parentDoc = await db.collection("users").doc(parentId).get();
      if (parentDoc.exists) {
        const parentFcm = parentDoc.data()?.fcmToken;
        if (parentFcm) {
          try {
            await messaging.send({
              token: parentFcm,
              notification: {
                title: "✅ XP Loan Sent",
                body: `${amount} XP lent to ${childName}. Auto-repayment: ${repayPct}%`,
              },
              data: {
                type: "XP_LOAN_CONFIRMED",
                userId: parentId,
                familyId: familyId,
                loanId: loanId,
                refreshTrigger: "true",
              },
              android: { priority: "high" },
            });
            console.log(`[XpLoan] Confirmation sent to parent: ${parentId}`);
          } catch (error: any) {
            if (error?.code === "messaging/registration-token-not-registered") {
              await db.collection("users").doc(parentId).update({ fcmToken: "" });
            }
          }
        }
      }
    } catch (error: any) {
      console.error(`[XpLoan] Error: ${error.message}`);
    }
  });

/**
 * XP Loan Repayment — triggered when a loan is updated (repayment or status change).
 * Notifies the parent when a loan is fully repaid.
 *
 * Firestore path: families/{familyId}/xp_loans/{loanId}
 */
export const notifyXpLoanUpdate = functions.firestore
  .document("families/{familyId}/xp_loans/{loanId}")
  .onUpdate(async (change: any, context: any) => {
    const before = change.before.data();
    const after = change.after.data();
    const { familyId, loanId } = context.params;

    if (!after || !after.parentId) return;

    // Only notify on status changes or significant repayment milestones
    const statusChanged = before.status !== after.status;
    const amountBefore = before.amountRepaid || 0;
    const amountAfter = after.amountRepaid || 0;
    const totalAmount = after.amount || 0;

    // Check if loan was just completed
    if (statusChanged && after.status === "COMPLETED") {
      console.log(`[XpLoan] Loan ${loanId} fully repaid!`);

      try {
        const parentDoc = await db.collection("users").doc(after.parentId).get();
        const parentFcm = parentDoc.data()?.fcmToken;
        if (parentFcm) {
          await messaging.send({
            token: parentFcm,
            notification: {
              title: "💰 Loan Fully Repaid!",
              body: `${after.childName} has fully repaid the ${totalAmount} XP loan! 🎉`,
            },
            data: {
              type: "XP_LOAN_REPAID",
              userId: after.parentId,
              familyId: familyId,
              loanId: loanId,
              refreshTrigger: "true",
            },
            android: { priority: "high" },
          });
          console.log(`[XpLoan] Repayment notification sent to parent: ${after.parentId}`);
        }
      } catch (error: any) {
        console.error(`[XpLoan] Repayment notification error: ${error.message}`);
      }
    }

    // Notify on 50% repayment milestone
    if (totalAmount > 0 && amountBefore < totalAmount * 0.5 && amountAfter >= totalAmount * 0.5) {
      try {
        const parentDoc = await db.collection("users").doc(after.parentId).get();
        const parentFcm = parentDoc.data()?.fcmToken;
        if (parentFcm) {
          await messaging.send({
            token: parentFcm,
            notification: {
              title: "📊 Loan 50% Repaid",
              body: `${after.childName} has repaid half of their ${totalAmount} XP loan!`,
            },
            data: {
              type: "XP_LOAN_MILESTONE",
              userId: after.parentId,
              familyId: familyId,
              refreshTrigger: "true",
            },
            android: { priority: "normal" },
          });
        }
      } catch (error: any) {
        console.error(`[XpLoan] Milestone notification error: ${error.message}`);
      }
    }

    // Check if loan was forgiven — notify the child
    if (statusChanged && after.status === "FORGIVEN") {
      try {
        const childDoc = await db.collection("users").doc(after.childId).get();
        const childFcm = childDoc.data()?.fcmToken;
        if (childFcm) {
          const remaining = totalAmount - amountAfter;
          await messaging.send({
            token: childFcm,
            notification: {
              title: "🎁 Loan Forgiven!",
              body: `Your parent forgave ${remaining} XP of your loan! You don't owe it anymore.`,
            },
            data: {
              type: "XP_LOAN_FORGIVEN",
              userId: after.childId,
              familyId: familyId,
              refreshTrigger: "true",
            },
            android: { priority: "high" },
          });
        }
      } catch (error: any) {
        console.error(`[XpLoan] Forgiven notification error: ${error.message}`);
      }
    }
  });
