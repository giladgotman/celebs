package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import io.reactivex.Completable
import javax.inject.Inject

class EndTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Completable {
        return setGame(
            game
                .setTurnState(TurnState.Stopped)
                .setCurrentCard(null)
                .setTurnPlayer(null)
        )
    }
}

