package com.kidsroutine.core.model

import com.google.firebase.firestore.PropertyName

data class TaskTemplate(
    val templateId: String = "",
    @PropertyName("familyId")
    val familyId: String? = null,        // ← ADD THIS LINE
    val generationParams: Map<String, Any> = emptyMap(),
    val baseTask: TaskModel = TaskModel()
)
