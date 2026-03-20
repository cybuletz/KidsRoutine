package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Public API for all task-related operations.
 * Features MUST call this — never bypass it.
 */
@Singleton
class TaskEngine @Inject constructor(
    val generator: TaskGenerator,
    val validator: TaskValidator
) {
    fun generateDailyTasks(
        templates: List<TaskTemplate>,
        injected: List<TaskInstance>,
        context: GenerationContext
    ): List<TaskInstance> = generator.generate(templates, injected, context)

    fun validate(
        task: TaskModel,
        progress: TaskProgressModel,
        photoUrl: String? = null
    ): ValidationResult = validator.validate(task, progress, photoUrl)
}
