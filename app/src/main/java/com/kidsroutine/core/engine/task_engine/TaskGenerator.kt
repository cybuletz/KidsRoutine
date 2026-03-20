package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.*
import com.kidsroutine.core.common.util.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

data class GenerationContext(
    val userId: String,
    val date: String,
    val recentTemplateIds: List<String>,      // anti-repetition
    val activeChallengeTaskIds: List<String>, // injected from ChallengeEngine
    val userPreferences: UserPreferences
)

@Singleton
class TaskGenerator @Inject constructor() {

    companion object {
        const val DAILY_TASK_LIMIT = 5
        const val MIN_COOP_TASKS = 1
        const val MIN_REAL_LIFE_TASKS = 2
        const val MIN_LOGIC_OR_LEARNING = 1
    }

    /**
     * Selects exactly DAILY_TASK_LIMIT tasks from available templates.
     * Injected challenge tasks count toward the limit.
     * Enforces composition constraints and anti-repetition.
     */
    fun generate(
        availableTemplates: List<TaskTemplate>,
        injectedTasks: List<TaskInstance>,     // from challenge engine
        context: GenerationContext
    ): List<TaskInstance> {
        val result = mutableListOf<TaskInstance>()
        result.addAll(injectedTasks.take(DAILY_TASK_LIMIT))

        val remaining = DAILY_TASK_LIMIT - result.size
        if (remaining <= 0) return result.take(DAILY_TASK_LIMIT)

        val eligible = availableTemplates
            .filter { it.templateId !in context.recentTemplateIds }
            .filter { it.baseTask.type in TaskType.entries }
            .shuffled()

        val coopNeeded   = (MIN_COOP_TASKS - result.count { it.task.requiresCoop }).coerceAtLeast(0)
        val realLifeNeeded = (MIN_REAL_LIFE_TASKS - result.count { it.task.type == TaskType.REAL_LIFE }).coerceAtLeast(0)
        val logicNeeded  = (MIN_LOGIC_OR_LEARNING - result.count {
            it.task.type == TaskType.LOGIC || it.task.type == TaskType.LEARNING }).coerceAtLeast(0)

        fun pickFirst(predicate: (TaskTemplate) -> Boolean): TaskInstance? =
            eligible.firstOrNull(predicate)?.toInstance(context.userId, context.date)

        repeat(coopNeeded)    { pickFirst { it.baseTask.requiresCoop }?.let { result.add(it) } }
        repeat(realLifeNeeded){ pickFirst { it.baseTask.type == TaskType.REAL_LIFE && !it.baseTask.requiresCoop }?.let { result.add(it) } }
        repeat(logicNeeded)   { pickFirst { it.baseTask.type == TaskType.LOGIC || it.baseTask.type == TaskType.LEARNING }?.let { result.add(it) } }

        // fill remainder with any eligible not already added
        val usedIds = result.map { it.templateId }.toSet()
        eligible.filter { it.templateId !in usedIds }.forEach {
            if (result.size < DAILY_TASK_LIMIT) result.add(it.toInstance(context.userId, context.date))
        }

        return result.take(DAILY_TASK_LIMIT)
    }
}

private fun TaskTemplate.toInstance(userId: String, date: String) = TaskInstance(
    instanceId  = "${templateId}_${userId}_${date}",
    templateId  = templateId,
    task        = baseTask,
    assignedDate = date,
    userId      = userId
)
