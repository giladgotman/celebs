package com.gggames.celebs.data.model

data class Game (val id: String,
                 val name: String,
                 val createdAt: Long,
                 val celebsCount: Int = 6,
                 val groups: List<Group> = emptyList(),
                 val rounds: List<Round> = defaultRoundsList(),
                 val state: GameState
)

data class GameInfo(
    private val round: Round,
    private val score: Map<Group, Int>,
    private val totalCards: Int,
    private val cardsInDeck: Int,
    private val currentPlayer: Player
)

sealed class GameState {
    data class Created(
        private val myCards: List<Card>,
        private val otherCardsCount: Map<Player, Int>
    ): GameState()

    data class Ready(private val gameInfo: GameInfo): GameState()

    data class Started(private val gameInfo: GameInfo): GameState()

    data class Finished(private val gameInfo: GameInfo): GameState()
}
