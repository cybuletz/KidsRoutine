package com.kidsroutine.feature.parent.data

import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.XpLoan
import kotlinx.coroutines.flow.Flow

/**
 * Repository for parent control settings and XP bank operations.
 * Backs Firestore collections:
 *   - families/{familyId}/parent_controls/{childId}
 *   - families/{familyId}/xp_loans/{loanId}
 */
interface ParentControlRepository {
    // ── Parent Control Settings ────────────────────────────────────
    suspend fun getControlSettings(familyId: String, childId: String): ParentControlSettings
    suspend fun saveControlSettings(settings: ParentControlSettings)
    fun observeControlSettings(familyId: String, childId: String): Flow<ParentControlSettings>

    // ── XP Bank / Loans ────────────────────────────────────────────
    suspend fun createLoan(loan: XpLoan)
    suspend fun getActiveLoans(familyId: String, childId: String): List<XpLoan>
    suspend fun getAllFamilyLoans(familyId: String): List<XpLoan>
    suspend fun repayLoan(loanId: String, familyId: String, amount: Int)
    suspend fun forgiveLoan(loanId: String, familyId: String)
    suspend fun cancelLoan(loanId: String, familyId: String)
    fun observeActiveLoans(familyId: String, childId: String): Flow<List<XpLoan>>
}
