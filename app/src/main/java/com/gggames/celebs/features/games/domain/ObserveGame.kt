package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.presentation.gameon.GameScreenContract
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import javax.inject.Inject

class ObserveGame @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String): Observable<GameScreenContract.Result.GameResult> =
        gamesRepository.observeGame(gameId)
            .compose(schedulerProvider.applyDefault())
            .map { GameScreenContract.Result.GameResult(it) }
}