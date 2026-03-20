package com.kidsroutine.feature.daily.domain

import com.kidsroutine.core.model.DailyStateModel
import com.kidsroutine.feature.daily.data.DailyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyStateUseCase @Inject constructor(
    private val repository: DailyRepository
) {
    operator fun invoke(userId: String, date: String): Flow<DailyStateModel> =
        repository.observeDailyState(userId, date)
}
