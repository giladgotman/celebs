package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.TurnState
import com.gggames.hourglass.model.setTurnState
import com.gggames.hourglass.model.setTurnTime
import javax.inject.Inject

class PauseTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game, time: Long?) =
        setGame(
            game
                .setTurnState(TurnState.Paused)
                .setTurnTime(time ?: game.turn.time),
            this.javaClass.simpleName
        )
}

