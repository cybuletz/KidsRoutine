package com.kidsroutine.navigation

object Routes {
    const val SPLASH      = "splash"
    const val DAILY       = "daily"
    const val EXECUTION   = "execution/{taskId}"
    const val PARENT      = "parent"
    const val FAMILY      = "family"
    const val CHALLENGES  = "challenges"
    const val CHALLENGE_DETAIL = "challenge/{challengeId}"
    const val COMMUNITY   = "community"
    const val STATS       = "stats"
    const val SETTINGS    = "settings"

    // helpers to build routes with args
    fun execution(taskId: String)        = "execution/$taskId"
    fun challengeDetail(challengeId: String) = "challenge/$challengeId"
}
