package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.setTurnTime
import javax.inject.Inject

class UpdateTurnTime @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(time: Long) =
        setGame(
            { currentGame ->
                currentGame
                    .setTurnTime(time)
            },
            this.javaClass.simpleName
        )
}

