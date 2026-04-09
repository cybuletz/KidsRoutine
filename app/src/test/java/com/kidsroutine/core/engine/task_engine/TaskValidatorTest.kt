package com.kidsroutine.core.engine.task_engine

import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.TaskProgressModel
import com.kidsroutine.core.model.ValidationType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskValidatorTest {

    private lateinit var validator: TaskValidator

    @Before
    fun setUp() {
        validator = TaskValidator()
    }

    @Test
    fun `AUTO validation type always approves`() {
        val task = TaskModel(validationType = ValidationType.AUTO)
        val result = validator.validate(task, TaskProgressModel())
        assertTrue(result is ValidationResult.Approved)
    }

    @Test
    fun `SELF validation type always approves`() {
        val task = TaskModel(validationType = ValidationType.SELF)
        val result = validator.validate(task, TaskProgressModel())
        assertTrue(result is ValidationResult.Approved)
    }

    @Test
    fun `PHOTO_REQUIRED rejects when no photo provided`() {
        val task = TaskModel(validationType = ValidationType.PHOTO_REQUIRED)
        val result = validator.validate(task, TaskProgressModel(), photoUrl = null)
        assertTrue(result is ValidationResult.Rejected)
    }

    @Test
    fun `PHOTO_REQUIRED rejects when blank photo`() {
        val task = TaskModel(validationType = ValidationType.PHOTO_REQUIRED)
        val result = validator.validate(task, TaskProgressModel(), photoUrl = "")
        assertTrue(result is ValidationResult.Rejected)
    }

    @Test
    fun `PHOTO_REQUIRED approves when photo provided`() {
        val task = TaskModel(validationType = ValidationType.PHOTO_REQUIRED)
        val result = validator.validate(task, TaskProgressModel(), photoUrl = "https://example.com/photo.jpg")
        assertTrue(result is ValidationResult.Approved)
    }

    @Test
    fun `PARENT_REQUIRED returns PendingParent`() {
        val task = TaskModel(validationType = ValidationType.PARENT_REQUIRED)
        val result = validator.validate(task, TaskProgressModel())
        assertTrue(result is ValidationResult.PendingParent)
    }

    @Test
    fun `HYBRID rejects when no photo`() {
        val task = TaskModel(validationType = ValidationType.HYBRID)
        val result = validator.validate(task, TaskProgressModel(), photoUrl = null)
        assertTrue(result is ValidationResult.Rejected)
    }

    @Test
    fun `HYBRID returns PendingParent when photo provided`() {
        val task = TaskModel(validationType = ValidationType.HYBRID)
        val result = validator.validate(task, TaskProgressModel(), photoUrl = "https://example.com/photo.jpg")
        assertTrue(result is ValidationResult.PendingParent)
    }
}
