package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import com.gggames.celebs.presentation.gameon.TURN_TIME_MILLIS
import io.reactivex.Observable
import javax.inject.Inject

class EndTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Observable<SetGameResult> {
        val endTurnGame = game
            .setTurnState(TurnState.Over)
            .setCurrentCard(null)
            .setTurnTime(TURN_TIME_MILLIS)
            .setTurnPlayer(null)

        return setGame(
            endTurnGame,
            "${this.javaClass.simpleName}.setTurnEnded"
        ).filter { it is Done }
            .switchMap {
                setGame(
                    endTurnGame.setTurnState(TurnState.Idle),
                    "${this.javaClass.simpleName}.setTurnIdle"
                )
            }
    }
}

