package com.kidsroutine.core.model

/**
 * Represents an XP loan from parent to child (like a bank).
 * Stored in Firestore: families/{familyId}/xp_loans/{loanId}
 *
 * Parents can "lend" XP to children who can spend it immediately.
 * Children repay through completing tasks (automatic % deduction or manual).
 */
data class XpLoan(
    val loanId: String = "",
    val familyId: String = "",
    val parentId: String = "",
    val childId: String = "",
    val childName: String = "",

    val amount: Int = 0,                          // Total XP lent
    val amountRepaid: Int = 0,                    // XP repaid so far
    val repaymentPercentage: Int = 20,            // % of task XP auto-deducted for repayment (0 = manual only)
    val status: XpLoanStatus = XpLoanStatus.ACTIVE,
    val note: String = "",                        // Parent's note to child

    val createdAt: Long = 0L,
    val completedAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val remaining: Int get() = (amount - amountRepaid).coerceAtLeast(0)
    val isFullyRepaid: Boolean get() = amountRepaid >= amount
    val progressPercent: Float get() = if (amount > 0) amountRepaid.toFloat() / amount else 0f
}

enum class XpLoanStatus {
    ACTIVE,       // Loan is active, child is repaying
    COMPLETED,    // Fully repaid
    FORGIVEN,     // Parent forgave remaining balance
    CANCELLED     // Parent cancelled the loan
}
