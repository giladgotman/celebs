package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.TurnState
import com.gggames.celebs.model.setTurnState
import com.gggames.celebs.model.setTurnTime
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

