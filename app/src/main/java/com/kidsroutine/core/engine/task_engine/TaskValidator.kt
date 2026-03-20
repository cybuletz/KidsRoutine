package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskProgressModel
import com.kidsroutine.core.model.ValidationType
import com.kidsroutine.core.model.ValidationStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskValidator @Inject constructor() {

    /**
     * Determines whether a task completion attempt is valid.
     * All validation logic lives HERE — never in UI.
     */
    fun validate(
        task: TaskModel,
        progress: TaskProgressModel,
        photoUrl: String? = null
    ): ValidationResult {
        return when (task.validationType) {
            ValidationType.AUTO -> ValidationResult.Approved
            ValidationType.SELF -> ValidationResult.Approved
            ValidationType.PHOTO_REQUIRED -> {
                if (photoUrl.isNullOrBlank())
                    ValidationResult.Rejected("Photo required to complete this task")
                else
                    ValidationResult.Approved
            }
            ValidationType.PARENT_REQUIRED -> ValidationResult.PendingParent
            ValidationType.HYBRID -> {
                if (photoUrl.isNullOrBlank())
                    ValidationResult.Rejected("Photo required before parent review")
                else
                    ValidationResult.PendingParent
            }
        }
    }
}

sealed class ValidationResult {
    data object Approved : ValidationResult()
    data object PendingParent : ValidationResult()
    data class Rejected(val reason: String) : ValidationResult()
}
