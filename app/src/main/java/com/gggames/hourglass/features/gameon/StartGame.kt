package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.*
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

