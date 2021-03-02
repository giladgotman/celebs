package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.CalculateNextPlayer
import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import com.gggames.hourglass.presentation.gameon.TURN_TIME_MILLIS
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class EndTurn @Inject constructor(
    private val setGame: SetGame,
    private val calculateNextPlayer: CalculateNextPlayer
) {
    operator fun invoke(teams: List<Team>): Observable<SetGameResult> {
        val endTurnGame = { game: Game ->
            val nextPlayer = calculateNextPlayer(teams, game.turn.player?.team)
            Timber.w("--- endTurn nextPlayer: $nextPlayer")
            game.setTurnState(TurnState.Over)
                .setCurrentCard(null)
                .setTurnTime(TURN_TIME_MILLIS)
                .setTurnPlayer(null)
                .setTurnNextPlayer(nextPlayer)
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

