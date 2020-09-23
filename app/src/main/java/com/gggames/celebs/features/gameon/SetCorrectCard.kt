package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.addCardsFoundInTurn
import io.reactivex.Completable
import javax.inject.Inject

class SetCorrectCard @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(card: Card, game: Game): Completable {
        return setGame(
            game
                .addCardsFoundInTurn(card)
        )
    }
}

