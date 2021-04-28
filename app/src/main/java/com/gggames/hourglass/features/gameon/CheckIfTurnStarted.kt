package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.ObserveGame
import com.gggames.hourglass.model.TurnState
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import io.reactivex.ObservableSource
import javax.inject.Inject

/**
 * Emits #StartedGameResult if turn state changed to Running
 */
class CheckIfTurnStarted @Inject constructor(val gamesRepository: GamesRepository, val observeGame: ObserveGame) {
    operator fun invoke(): ObservableSource<out GameScreenContract.Result>? {
        return gamesRepository.getCurrentGame().toObservable().switchMap { game ->
            observeGame(game.id)
                .buffer(2, 1).filter { buf ->
                    buf.size == 2 &&
                            (buf[0].game.turn.state != TurnState.Running && buf[1].game.turn.state == TurnState.Running)
                }
                .map { GameScreenContract.Result.StartedGameResult }
        }
    }
}