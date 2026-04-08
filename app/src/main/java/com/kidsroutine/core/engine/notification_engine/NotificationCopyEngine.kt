package com.kidsroutine.core.engine.notification_engine

import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.core.model.League
import com.kidsroutine.core.model.RooDialogue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart Notification Engine — context-aware, personality-driven notifications.
 * Replaces functional notifications with Roo-personified messages.
 * Selects templates based on user context (streak, league, pet, age).
 */
@Singleton
class NotificationCopyEngine @Inject constructor() {

    /**
     * Generate notification for streak at risk (evening).
     */
    fun streakAtRisk(streak: Int, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Roo misses you! 🥺"
                AgeGroup.EXPLORER -> "Streak alert! ⚠️"
                AgeGroup.TRAILBLAZER -> "Don't lose it 🔥"
                AgeGroup.LEGEND -> "Streak at risk"
            },
            body = RooDialogue.streakAtRisk(streak, ageGroup),
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Generate notification for absence (2+ days).
     */
    fun comebackNudge(daysAbsent: Int, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Roo is waiting! 🦘💕"
                AgeGroup.EXPLORER -> "Your adventure awaits! 🗺️"
                AgeGroup.TRAILBLAZER -> "Been a minute 👋"
                AgeGroup.LEGEND -> "Check in"
            },
            body = RooDialogue.comebackMessage(daysAbsent, ageGroup),
            priority = NotificationPriority.MEDIUM
        )
    }

    /**
     * Generate notification for pet needing attention.
     */
    fun petHungry(petName: String, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "$petName needs you! 🐾"
                AgeGroup.EXPLORER -> "$petName is hungry! 🦴"
                AgeGroup.TRAILBLAZER -> "$petName 👀"
                AgeGroup.LEGEND -> "$petName needs food"
            },
            body = RooDialogue.petHungry(petName, ageGroup),
            priority = NotificationPriority.MEDIUM
        )
    }

    /**
     * Generate notification for league promotion opportunity.
     */
    fun leaguePromotion(league: League, xpNeeded: Int, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Almost there! 🌟"
                AgeGroup.EXPLORER -> "${league.emoji} Promotion time!"
                AgeGroup.TRAILBLAZER -> "${league.displayName} is right there"
                AgeGroup.LEGEND -> "Promotion within reach"
            },
            body = RooDialogue.almostPromoted(league.displayName, xpNeeded, ageGroup),
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Generate notification for league demotion danger.
     */
    fun leagueDemotion(league: League, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Oh no! 😰"
                AgeGroup.EXPLORER -> "Danger zone! ⚠️"
                AgeGroup.TRAILBLAZER -> "Demotion incoming 📉"
                AgeGroup.LEGEND -> "Demotion risk"
            },
            body = RooDialogue.dangerZone(league.displayName, ageGroup),
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Generate notification for boss battle appearance.
     */
    fun bossAppeared(bossName: String, ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Boss Alert! 👾"
                AgeGroup.EXPLORER -> "⚔️ Boss Battle!"
                AgeGroup.TRAILBLAZER -> "Family boss incoming"
                AgeGroup.LEGEND -> "Boss: $bossName"
            },
            body = RooDialogue.bossAppeared(bossName, ageGroup),
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Generate notification for friend activity.
     */
    fun friendActivity(friendName: String, xpEarned: Int, ageGroup: AgeGroup): NotificationCopy {
        val body = when (ageGroup) {
            AgeGroup.SPROUT -> "🦘 $friendName just earned $xpEarned XP! Can you do even better?"
            AgeGroup.EXPLORER -> "🦘 $friendName just earned $xpEarned XP. Are you going to let them win?"
            AgeGroup.TRAILBLAZER -> "🦘 $friendName: $xpEarned XP. Your move."
            AgeGroup.LEGEND -> "🦘 $friendName earned $xpEarned XP today."
        }
        return NotificationCopy(
            title = "$friendName is on fire! 🔥",
            body = body,
            priority = NotificationPriority.LOW
        )
    }

    /**
     * Generate random encouragement notification.
     */
    fun randomEncouragement(ageGroup: AgeGroup): NotificationCopy {
        return NotificationCopy(
            title = when (ageGroup) {
                AgeGroup.SPROUT -> "Roo says hi! 🦘"
                AgeGroup.EXPLORER -> "Quick reminder! ⚡"
                AgeGroup.TRAILBLAZER -> "Hey 👋"
                AgeGroup.LEGEND -> "Daily note"
            },
            body = RooDialogue.randomEncouragement(ageGroup),
            priority = NotificationPriority.LOW
        )
    }

    /**
     * Generate notification for timed event start.
     */
    fun eventStarted(eventTitle: String, eventEmoji: String, ageGroup: AgeGroup): NotificationCopy {
        val body = when (ageGroup) {
            AgeGroup.SPROUT -> "🦘🎉 $eventEmoji A special event just started: $eventTitle! Let's GO!"
            AgeGroup.EXPLORER -> "🦘⚡ $eventEmoji New event: $eventTitle! Exclusive rewards await!"
            AgeGroup.TRAILBLAZER -> "🦘 $eventEmoji $eventTitle just dropped. Exclusive rewards."
            AgeGroup.LEGEND -> "🦘 $eventEmoji Event: $eventTitle — time-limited rewards available."
        }
        return NotificationCopy(
            title = "$eventEmoji $eventTitle starts now!",
            body = body,
            priority = NotificationPriority.HIGH
        )
    }

    /**
     * Generate notification for daily spin availability.
     */
    fun dailySpinAvailable(ageGroup: AgeGroup): NotificationCopy {
        val body = when (ageGroup) {
            AgeGroup.SPROUT -> "🦘🎰 Roo has a surprise! Complete your first task to spin the wheel!"
            AgeGroup.EXPLORER -> "🦘🎡 Daily spin is ready! What will you get today?"
            AgeGroup.TRAILBLAZER -> "🦘 Daily spin loaded. Could be 2x XP."
            AgeGroup.LEGEND -> "🦘 Daily spin available."
        }
        return NotificationCopy(
            title = "🎰 Daily Spin Ready!",
            body = body,
            priority = NotificationPriority.LOW
        )
    }
}

data class NotificationCopy(
    val title: String = "",
    val body: String = "",
    val priority: NotificationPriority = NotificationPriority.MEDIUM
)

enum class NotificationPriority { LOW, MEDIUM, HIGH }
