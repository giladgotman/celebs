package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import javax.inject.Inject

class ResumeTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game) =
        setGame(
            game
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running),
            this.javaClass.simpleName
        )
}

