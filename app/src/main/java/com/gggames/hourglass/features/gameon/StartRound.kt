package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.RoundState
import com.gggames.hourglass.model.setRoundState
import javax.inject.Inject

class StartRound @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game) =
        setGame(
            game
                .setRoundState(RoundState.Started),
            this.javaClass.simpleName
        )
}

