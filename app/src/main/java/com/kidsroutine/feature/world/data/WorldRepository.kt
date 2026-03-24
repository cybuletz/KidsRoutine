package com.kidsroutine.feature.world.data

import com.kidsroutine.core.model.WorldModel

interface WorldRepository {
    suspend fun getWorld(userXp: Int): WorldModel
}