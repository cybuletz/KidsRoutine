package com.kidsroutine.feature.tasks.ui

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RefreshEventManager {
    private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
    val refreshEvent: SharedFlow<Unit> = _refreshEvent.asSharedFlow()

    suspend fun triggerRefresh() {
        Log.d("RefreshEventManager", "Refresh triggered!")
        _refreshEvent.emit(Unit)
    }
}