package com.gggames.celebs.data.model

data class Game (val id: String,
                 val name: String,
                 val createdAt: Long,
                 val celebsCount: Int = 6,
                 val teams: List<Team> = emptyList(),
                 val rounds: List<Round> = defaultRoundsList(),
                 val players: List<Player> = emptyList(),
                 val state: GameState = GameState.Empty
)

data class GameInfo(
    val round: Round = Round.Speaking,
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val currentPlayer: Player
)

sealed class GameState {
    object Empty : GameState()
    data class Created(
        val myCards: List<Card>,
        val otherCardsCount: Map<String, Int>
    ): GameState()

    data class Ready(val gameInfo: GameInfo): GameState()

    data class Started(val gameInfo: GameInfo): GameState()

    data class Finished(val gameInfo: GameInfo): GameState()
}
