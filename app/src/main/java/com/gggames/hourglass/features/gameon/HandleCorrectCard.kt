package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.Card
import com.gggames.hourglass.model.addCardsFoundInTurn
import com.gggames.hourglass.model.increaseScore
import javax.inject.Inject

class HandleCorrectCard @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(card: Card, teamName: String) =
        setGame(
            { currentGame ->
                currentGame
                    .addCardsFoundInTurn(card)
                    .increaseScore(teamName)
            },
            this.javaClass.simpleName
        )
}

