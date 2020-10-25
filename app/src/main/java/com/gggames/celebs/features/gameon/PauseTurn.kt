package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.TurnState
import com.gggames.celebs.model.setTurnState
import javax.inject.Inject

class PauseTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game) =
        setGame(
            game.setTurnState(TurnState.Paused),
            this.javaClass.simpleName
        )
}

