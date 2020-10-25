package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.GameUpdate
import io.reactivex.Observable
import javax.inject.Inject

class ObserveGame @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(gameId: String): Observable<GameUpdate> =
        gamesRepository.observeGame(gameId)
            .distinctUntilChanged()
            .map { GameUpdate(it) }
}
