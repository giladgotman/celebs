package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import io.reactivex.Completable
import javax.inject.Inject

class StartGame @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(player: Player, game: Game): Completable {
        return setGame(
            game
                .setGameState(GameState.Started)
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running)
                .setTurnPlayer(player)
                .setTurnLastCards(emptyList())
        )
    }
}

