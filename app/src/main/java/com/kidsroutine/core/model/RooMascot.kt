package com.kidsroutine.core.model

/**
 * Roo the Kangaroo — App mascot with personality.
 * "Roo" = "Routine" → "Roo-tine" — the Duo the Owl equivalent.
 *
 * Age-adaptive personality: silly for younger kids, witty/sarcastic for teens.
 */

enum class RooExpression(val emoji: String, val description: String) {
    HAPPY("🦘😊", "Jumping with joy"),
    EXCITED("🦘🤩", "Eyes sparkling, bouncing"),
    PROUD("🦘😤", "Puffed up chest, fist pump"),
    WORRIED("🦘😟", "Wringing paws, looking around"),
    SAD("🦘😢", "Ears drooping, tiny tear"),
    SLEEPY("🦘😴", "Curled up, snoring"),
    SARCASTIC("🦘😏", "One eyebrow raised, smirk"),
    CELEBRATING("🦘🎉", "Confetti, party hat"),
    ENCOURAGING("🦘💪", "Flexing, thumbs up"),
    THINKING("🦘🤔", "Chin on paw, looking up")
}

data class RooState(
    val currentExpression: RooExpression = RooExpression.HAPPY,
    val seasonalOutfit: Season = Season.NONE,  // matches current seasonal theme
    val ageGroupPersonality: AgeGroup = AgeGroup.EXPLORER
) {
    /** Get Roo's display name based on familiarity */
    val displayName: String get() = "Roo"

    /** Get personality adjective based on age group */
    val personalityTone: String
        get() = when (ageGroupPersonality) {
            AgeGroup.SPROUT -> "playful"       // silly, encouraging, lots of emojis
            AgeGroup.EXPLORER -> "enthusiastic" // upbeat, motivating, adventure-themed
            AgeGroup.TRAILBLAZER -> "chill"     // witty, casual, sarcastic humor
            AgeGroup.LEGEND -> "respected"      // concise, smart, peer-like
        }
}

/**
 * Roo's personality-driven dialogue system.
 * Context-aware messages that make notifications engaging.
 */
object RooDialogue {

    // ── Streak messages ──────────────────────────────────────────────
    fun streakEncouragement(streak: Int, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> when {
            streak >= 30 -> "🦘🎉 WOW WOW WOW! $streak days! Roo is doing a happy dance!"
            streak >= 7  -> "🦘⭐ $streak days in a row! Roo thinks you're AMAZING!"
            else         -> "🦘✨ You did it! Day $streak! Roo is so proud of you!"
        }
        AgeGroup.EXPLORER -> when {
            streak >= 30 -> "🦘🔥 $streak-day streak! You're on fire! Even Roo can't keep up!"
            streak >= 7  -> "🦘💪 $streak days strong! Adventure mode: ACTIVATED!"
            else         -> "🦘⚡ Day $streak! Keep the momentum going, explorer!"
        }
        AgeGroup.TRAILBLAZER -> when {
            streak >= 30 -> "🦘 $streak days. That's not a streak, that's a lifestyle. Respect."
            streak >= 7  -> "🦘 $streak-day streak? Not bad. Let's see if you can double it."
            else         -> "🦘 Day $streak. Solid. Don't ghost me though."
        }
        AgeGroup.LEGEND -> when {
            streak >= 30 -> "🦘 $streak consecutive days. Impressive consistency."
            streak >= 7  -> "🦘 $streak-day streak. You've built the habit. Now maintain it."
            else         -> "🦘 Day $streak. Building momentum."
        }
    }

    // ── Streak at risk ───────────────────────────────────────────────
    fun streakAtRisk(streak: Int, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘😟 Roo has been staring at the door waiting for you all day! Your $streak-day streak needs you!"
        AgeGroup.EXPLORER -> "🦘⚠️ Your $streak-day streak is about to snap! Don't let the Distraction Dragon win!"
        AgeGroup.TRAILBLAZER -> "🦘 So... you're really gonna let a $streak-day streak die? Your call, I guess."
        AgeGroup.LEGEND -> "🦘 $streak-day streak at risk. One task is all it takes."
    }

    // ── Comeback after absence ───────────────────────────────────────
    fun comebackMessage(daysAbsent: Int, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘💕 YAY you're back! Roo missed you SO much! Let's play!"
        AgeGroup.EXPLORER -> "🦘👋 Welcome back, explorer! Roo kept your spot warm. Ready to jump back in?"
        AgeGroup.TRAILBLAZER -> "🦘 Oh, you remembered this app exists? Cool. No judgment. Let's go."
        AgeGroup.LEGEND -> "🦘 Good to see you. Let's get back on track — no pressure."
    }

    // ── Pet-related messages ─────────────────────────────────────────
    fun petHungry(petName: String, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘😢 $petName hasn't eaten today... Can you do a task to feed them?"
        AgeGroup.EXPLORER -> "🦘🦴 $petName is getting hungry! Complete a task to feed your buddy."
        AgeGroup.TRAILBLAZER -> "🦘 $petName is giving you the sad eyes. You know what to do."
        AgeGroup.LEGEND -> "🦘 $petName needs attention. A quick task will do."
    }

    // ── League-related messages ──────────────────────────────────────
    fun almostPromoted(league: String, xpNeeded: Int, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘🌟 You're SO close to $league league! Just $xpNeeded more XP!"
        AgeGroup.EXPLORER -> "🦘🔥 $xpNeeded XP from $league league. One more task could do it!"
        AgeGroup.TRAILBLAZER -> "🦘 $xpNeeded XP to $league. That's, like, one task. Do it."
        AgeGroup.LEGEND -> "🦘 $xpNeeded XP to $league promotion. Within reach."
    }

    fun dangerZone(league: String, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘😰 Oh no! You might drop from $league league! Quick, do a task!"
        AgeGroup.EXPLORER -> "🦘⚠️ Danger zone! You're about to get demoted from $league! Fight back!"
        AgeGroup.TRAILBLAZER -> "🦘 You're in the demotion zone for $league. Just saying."
        AgeGroup.LEGEND -> "🦘 Demotion risk from $league. One task prevents it."
    }

    // ── Boss battle messages ─────────────────────────────────────────
    fun bossAppeared(bossName: String, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘😲 The $bossName appeared! Quick, help your family fight it!"
        AgeGroup.EXPLORER -> "🦘⚔️ BOSS ALERT: The $bossName is attacking! Rally your family!"
        AgeGroup.TRAILBLAZER -> "🦘 The $bossName showed up. Your family needs backup."
        AgeGroup.LEGEND -> "🦘 Family boss: $bossName. Coordinate with your team."
    }

    // ── General encouragement ────────────────────────────────────────
    fun randomEncouragement(ageGroup: AgeGroup): String {
        val messages = when (ageGroup) {
            AgeGroup.SPROUT -> listOf(
                "🦘✨ You're doing GREAT! Roo believes in you!",
                "🦘🌈 Every task makes the world brighter!",
                "🦘🎉 You're a superstar! Keep going!",
                "🦘💕 Roo is your biggest fan!"
            )
            AgeGroup.EXPLORER -> listOf(
                "🦘🗺️ Every task is a step on your adventure!",
                "🦘💪 Stronger every day! Keep pushing!",
                "🦘⚡ Fun fact: You've completed more tasks than 80% of kids your age!",
                "🦘🏆 Champions are made one task at a time!"
            )
            AgeGroup.TRAILBLAZER -> listOf(
                "🦘 Consistency beats motivation. You're proving it.",
                "🦘 Small steps, big results. That's the trailblazer way.",
                "🦘 Plot twist: doing the thing is easier than thinking about doing the thing.",
                "🦘 Your future self is already thanking you."
            )
            AgeGroup.LEGEND -> listOf(
                "🦘 Discipline is freedom. Keep building.",
                "🦘 Excellence is a habit, not an act.",
                "🦘 Results speak. Yours are loud.",
                "🦘 One more task. That's all it ever takes."
            )
        }
        return messages.random()
    }

    // ── Avatar shop messages ─────────────────────────────────────────
    fun shopGreeting(ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘🛍️ Welcome to Roo's Shop! Look at all the pretty things!"
        AgeGroup.EXPLORER -> "🦘✨ Roo's got fresh gear! Check out what's new!"
        AgeGroup.TRAILBLAZER -> "🦘 New drops just landed. Check it."
        AgeGroup.LEGEND -> "🦘 Browse the collection."
    }

    // ── Game messages ────────────────────────────────────────────────
    fun gameIntro(gameName: String, ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.SPROUT -> "🦘🎮 Let's play $gameName! Roo loves this one!"
        AgeGroup.EXPLORER -> "🦘🎮 $gameName time! Roo challenges you to beat your record!"
        AgeGroup.TRAILBLAZER -> "🦘 $gameName. Think you can handle it?"
        AgeGroup.LEGEND -> "🦘 $gameName — go."
    }
}
