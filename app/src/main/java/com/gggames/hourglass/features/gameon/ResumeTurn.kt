package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.*
import javax.inject.Inject

class ResumeTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game, time: Long?) =
        setGame(
            game
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running)
                .setTurnTime(time ?: game.turn.time),
            this.javaClass.simpleName
        )
}

