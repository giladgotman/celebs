package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import com.gggames.hourglass.presentation.gameon.TURN_TIME_MILLIS
import io.reactivex.Observable
import javax.inject.Inject

class EndTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(): Observable<SetGameResult> {
        val endTurnGame = { game: Game ->
            game.setTurnState(TurnState.Over)
                .setCurrentCard(null)
                .setTurnTime(TURN_TIME_MILLIS)
                .setTurnPlayer(null)
        }

        return setGame(
            endTurnGame,
            "${this.javaClass.simpleName}.setTurnEnded"
        ).filter { it is Done }
            .switchMap {
                setGame(
                    { game: Game -> game.setTurnState(TurnState.Idle) },
                    "${this.javaClass.simpleName}.setTurnIdle"
                )
            }
    }
}

