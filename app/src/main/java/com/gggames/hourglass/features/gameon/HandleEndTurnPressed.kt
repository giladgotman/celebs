package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.TurnState
import com.gggames.hourglass.model.setTurnState
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.EndTurnPressedResult.ShowLeaveGameConfirmation
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class HandleEndTurnPressed @Inject constructor(
    private val authenticator: Authenticator,
    private val gamesRepository: GamesRepository,
    private val setGame: SetGame
) {
    operator fun invoke(): Observable<out GameScreenContract.Result> =
        gamesRepository.getCurrentGame().toObservable().switchMap { game ->
            if (authenticator.isMyselfActivePlayerBlocking(game)) {
                gamesRepository.getCurrentGame().toObservable()
                    .switchMap { game ->
                        setGame(game.setTurnState(TurnState.Paused))
                            .filter { it is Done }
                            .switchMap {
                                just(
                                    ShowLeaveGameConfirmation(true),
                                    ShowLeaveGameConfirmation(false)
                                )
                            }
                    }
            } else {
                just(
                    GameScreenContract.Result.NoOp
                )
            }
        }
}

