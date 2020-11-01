package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.GameUpdate
import io.reactivex.Observable
import javax.inject.Inject

class ObserveGame @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(gameId: String): Observable<GameUpdate> =
        gamesRepository.observeGame(gameId)
            .filter { it is GameResult.Found }
            .cast(GameResult.Found::class.java)
            .distinctUntilChanged()
            .map { GameUpdate(it.game) }
}
