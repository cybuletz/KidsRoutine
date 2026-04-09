package com.kidsroutine.feature.execution.domain

import com.kidsroutine.core.ai.AIGenerationService
import com.kidsroutine.core.ai.GenerationContext
import com.kidsroutine.core.ai.GenerationType
import com.kidsroutine.core.engine.progression_engine.ProgressionEngine
import com.kidsroutine.core.engine.progression_engine.StreakCalculator
import com.kidsroutine.core.engine.progression_engine.XpCalculator
import com.kidsroutine.core.engine.task_engine.TaskEngine
import com.kidsroutine.core.engine.task_engine.ValidationResult
import com.kidsroutine.core.model.*
import com.kidsroutine.feature.achievements.data.AchievementRepository
import com.kidsroutine.feature.daily.data.StoryArcRepository
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.execution.data.TaskProgressRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CompleteTaskUseCaseTest {

    private lateinit var taskEngine: TaskEngine
    private lateinit var progressionEngine: ProgressionEngine
    private lateinit var streakCalculator: StreakCalculator
    private lateinit var xpCalculator: XpCalculator
    private lateinit var repository: TaskProgressRepository
    private lateinit var userRepository: UserRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var storyArcRepository: StoryArcRepository
    private lateinit var taskInstanceDao: com.kidsroutine.core.database.dao.TaskInstanceDao
    private lateinit var aiGenerationService: AIGenerationService

    private lateinit var useCase: CompleteTaskUseCase

    private val userId = "user1"
    private val familyId = "family1"

    private fun baseTask(
        id: String = "task1",
        type: TaskType = TaskType.REAL_LIFE,
        title: String = "Brush Teeth",
        requiresCoop: Boolean = false,
        xp: Int = 10
    ) = TaskModel(
        id = id,
        type = type,
        title = title,
        requiresCoop = requiresCoop,
        reward = TaskReward(xp = xp)
    )

    @Before
    fun setUp() {
        taskEngine = mockk()
        streakCalculator = mockk()
        xpCalculator = mockk()
        progressionEngine = ProgressionEngine(xpCalculator, streakCalculator)
        repository = mockk(relaxUnitFun = true)
        userRepository = mockk(relaxUnitFun = true)
        achievementRepository = mockk()
        storyArcRepository = mockk()
        taskInstanceDao = mockk(relaxUnitFun = true)
        aiGenerationService = mockk()

        // Firestore mock chain
        val mockCollectionRef = mockk<CollectionReference>(relaxed = true)
        val mockDocRef = mockk<DocumentReference>(relaxed = true)
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        firestore = mockk()
        every { firestore.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document(any()) } returns mockDocRef
        every { mockDocRef.collection(any()) } returns mockCollectionRef
        every { mockDocRef.set(any()) } returns mockTask
        every { mockDocRef.delete() } returns mockTask
        every { mockDocRef.update(any<Map<String, Any>>()) } returns mockTask

        // Default stubs
        every { streakCalculator.computeStreak(any(), any(), any()) } returns 1
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { achievementRepository.checkAndUnlockAchievements(any()) } returns emptyList()
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("AI unavailable"))

        useCase = CompleteTaskUseCase(
            taskEngine,
            progressionEngine,
            repository,
            userRepository,
            firestore,
            achievementRepository,
            storyArcRepository,
            taskInstanceDao,
            aiGenerationService
        )
    }

    // ── 1. Rejected validation ──────────────────────────────────────────

    @Test
    fun `rejected validation returns CompletionResult Rejected with reason`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Rejected("photo required")

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Rejected)
        assertEquals("photo required", (result as CompletionResult.Rejected).reason)
    }

    // ── 2. Approved validation ──────────────────────────────────────────

    @Test
    fun `approved validation returns Success with xpGained and newStreak`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { xpCalculator.forTask(task, isCoop = false, isStreakBonus = false) } returns 15
        every { streakCalculator.computeStreak(0, "", any()) } returns 1

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Success)
        val success = result as CompletionResult.Success
        assertEquals(15, success.xpGained)
        assertEquals(1, success.newStreak)
        assertFalse(success.needsParent)
    }

    // ── 3. PendingParent validation ─────────────────────────────────────

    @Test
    fun `pending parent validation returns Success with needsParent true`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.PendingParent
        every { xpCalculator.forTask(task, isCoop = false, isStreakBonus = false) } returns 10
        every { streakCalculator.computeStreak(0, "", any()) } returns 1

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Success)
        assertTrue((result as CompletionResult.Success).needsParent)
    }

    // ── 4. XP calculation ───────────────────────────────────────────────

    @Test
    fun `xp calculator forTask is called and result is in CompletionResult`() = runTest {
        val task = baseTask(requiresCoop = true, xp = 20)
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(5, "2026-01-01", any()) } returns 6
        every { xpCalculator.forTask(task, isCoop = true, isStreakBonus = true) } returns 55

        val result = useCase(task, userId, familyId, currentStreak = 5, lastActiveDate = "2026-01-01")

        assertTrue(result is CompletionResult.Success)
        assertEquals(55, (result as CompletionResult.Success).xpGained)
        verify { xpCalculator.forTask(task, isCoop = true, isStreakBonus = true) }
    }

    // ── 5. Streak calculation ───────────────────────────────────────────

    @Test
    fun `streak calculator computeStreak is called with correct args`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(3, "2026-06-01", any()) } returns 4
        every { xpCalculator.forTask(task, isCoop = false, isStreakBonus = true) } returns 11

        val result = useCase(task, userId, familyId, currentStreak = 3, lastActiveDate = "2026-06-01")

        assertTrue(result is CompletionResult.Success)
        assertEquals(4, (result as CompletionResult.Success).newStreak)
        verify { streakCalculator.computeStreak(3, "2026-06-01", any()) }
    }

    // ── 6. defaultCelebration streak >= 7 → "on fire" ───────────────────

    @Test
    fun `celebration with streak 7 or more contains on fire`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(6, "2026-06-01", any()) } returns 7
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("fail"))

        val result = useCase(task, userId, familyId, currentStreak = 6, lastActiveDate = "2026-06-01", childName = "Liam")

        assertTrue(result is CompletionResult.Success)
        assertTrue((result as CompletionResult.Success).celebrationMessage.contains("on fire"))
    }

    // ── 7. defaultCelebration streak >= 3 < 7 → "Amazing" ──────────────

    @Test
    fun `celebration with streak between 3 and 6 contains Amazing`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(2, "2026-06-01", any()) } returns 3
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("fail"))

        val result = useCase(task, userId, familyId, currentStreak = 2, lastActiveDate = "2026-06-01", childName = "Mia")

        assertTrue(result is CompletionResult.Success)
        assertTrue((result as CompletionResult.Success).celebrationMessage.contains("Amazing"))
    }

    // ── 8. defaultCelebration streak < 3 → "Great job" ──────────────────

    @Test
    fun `celebration with streak less than 3 contains Great job`() = runTest {
        val task = baseTask(title = "Read a Book")
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(0, "", any()) } returns 1
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("fail"))

        val result = useCase(task, userId, familyId, childName = "Ella")

        assertTrue(result is CompletionResult.Success)
        val msg = (result as CompletionResult.Success).celebrationMessage
        assertTrue(msg.contains("Great job"))
        assertTrue(msg.contains("Read a Book"))
    }

    // ── 9. defaultCelebration with blank name → "Champion" ──────────────

    @Test
    fun `celebration with blank name uses Champion`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(0, "", any()) } returns 1
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("fail"))

        val result = useCase(task, userId, familyId, childName = "")

        assertTrue(result is CompletionResult.Success)
        assertTrue((result as CompletionResult.Success).celebrationMessage.contains("Champion"))
    }

    // ── 10. STORY task → storyArcRepository.advanceDay called ───────────

    @Test
    fun `story task advances story arc day`() = runTest {
        val task = baseTask(id = "story_space_day1", type = TaskType.STORY)
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        coEvery { storyArcRepository.getActiveArc(familyId) } returns StoryArc(
            arcId = "space",
            familyId = familyId,
            currentDay = 1,
            chapters = listOf(StoryChapter(day = 1), StoryChapter(day = 2), StoryChapter(day = 3))
        )
        coEvery { storyArcRepository.advanceDay("space") } just Runs

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Success)
        coVerify { storyArcRepository.advanceDay("space") }
    }

    // ── 11. STORY task last chapter → completeArc called ────────────────

    @Test
    fun `story task on last chapter completes arc`() = runTest {
        val task = baseTask(id = "story_adventure_day3", type = TaskType.STORY)
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        coEvery { storyArcRepository.getActiveArc(familyId) } returns StoryArc(
            arcId = "adventure",
            familyId = familyId,
            currentDay = 3,
            chapters = listOf(StoryChapter(day = 1), StoryChapter(day = 2), StoryChapter(day = 3))
        )
        coEvery { storyArcRepository.completeArc("adventure") } just Runs

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Success)
        coVerify { storyArcRepository.completeArc("adventure") }
    }

    // ── 12. Non-story task → storyArcRepository NOT called ──────────────

    @Test
    fun `non-story task does not touch story arc repository`() = runTest {
        val task = baseTask(type = TaskType.REAL_LIFE)
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved

        val result = useCase(task, userId, familyId)

        assertTrue(result is CompletionResult.Success)
        coVerify(exactly = 0) { storyArcRepository.getActiveArc(any()) }
        coVerify(exactly = 0) { storyArcRepository.advanceDay(any()) }
        coVerify(exactly = 0) { storyArcRepository.completeArc(any()) }
    }

    // ── 13. AI celebration fails → falls back to default ────────────────

    @Test
    fun `ai celebration failure falls back to default celebration`() = runTest {
        val task = baseTask(title = "Tidy Room")
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(0, "", any()) } returns 1
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery { aiGenerationService.generate(any(), any(), any(), any(), any()) } returns Result.failure(Exception("network error"))

        val result = useCase(task, userId, familyId, childName = "Sam")

        assertTrue(result is CompletionResult.Success)
        val msg = (result as CompletionResult.Success).celebrationMessage
        assertTrue(msg.contains("Great job"))
        assertTrue(msg.contains("Sam"))
    }

    // ── 14. Achievement check after completion ──────────────────────────

    @Test
    fun `achievement check is called after successful completion`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        coEvery { achievementRepository.checkAndUnlockAchievements(userId) } returns listOf(
            Badge(id = "b1", type = AchievementType.TASKS_COMPLETED_10, title = "10 Tasks!", isUnlocked = true)
        )

        useCase(task, userId, familyId)

        coVerify { achievementRepository.checkAndUnlockAchievements(userId) }
    }

    // ── 15. AI celebration success overrides default ─────────────────────

    @Test
    fun `ai celebration success overrides default celebration message`() = runTest {
        val task = baseTask()
        every { taskEngine.validate(task, any(), any()) } returns ValidationResult.Approved
        every { streakCalculator.computeStreak(0, "", any()) } returns 1
        every { xpCalculator.forTask(any(), any(), any()) } returns 10
        coEvery {
            aiGenerationService.generate(any(), eq(GenerationType.CUSTOM), any(), eq(60), eq(0.9f))
        } returns Result.success("🌟 Way to go, superstar!")

        val result = useCase(task, userId, familyId, childName = "Alex")

        assertTrue(result is CompletionResult.Success)
        assertEquals("🌟 Way to go, superstar!", (result as CompletionResult.Success).celebrationMessage)
    }
}
