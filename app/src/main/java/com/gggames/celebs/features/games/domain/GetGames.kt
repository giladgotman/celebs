package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.GameState
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import javax.inject.Inject

/*
Gets games that are Created or Started
 */
class GetGames @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke() = gamesRepository.getGames(
        emptyList(),
        listOf(GameState.Created, GameState.Started)
    ).compose(schedulerProvider.applyDefault())
}
