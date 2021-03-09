package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.TurnState
import com.gggames.hourglass.model.setTurnState
import com.gggames.hourglass.model.setTurnTime
import javax.inject.Inject

class PauseTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(time: Long?) =
        setGame(
            { currentGame ->
                currentGame
                    .setTurnState(TurnState.Paused)
                    .setTurnTime(time ?: currentGame.turn.time)
            },
            this.javaClass.simpleName
        )
}

