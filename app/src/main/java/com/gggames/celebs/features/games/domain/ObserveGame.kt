package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.Game
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable

class ObserveGame(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String): Observable<Game> =
        gamesRepository.observeGame(gameId)
            .compose(schedulerProvider.applyDefault())
}