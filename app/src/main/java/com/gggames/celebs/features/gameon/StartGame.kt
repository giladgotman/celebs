package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import javax.inject.Inject

class StartGame @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(player: Player, game: Game) =
        setGame(
            game
                .setGameState(GameState.Started)
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running)
                .resetCardsFoundInTurn()
                .setTurnPlayer(player)
                .setTeamLastPlayerId(player)
                .setTurnLastCards(emptyList()),
            this.javaClass.simpleName
        )
}

