package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.addCardsFoundInTurn
import com.gggames.celebs.model.increaseScore
import io.reactivex.Completable
import javax.inject.Inject

class HandleCorrectCard @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(card: Card, game: Game, teamName: String): Completable {
        return setGame(
            game
                .addCardsFoundInTurn(card)
                .increaseScore(teamName)
        )
    }
}

