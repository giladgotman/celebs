package com.gggames.celebs.data.model

data class Game (val id: String,
                 val name: String,
                 val createdAt: Long,
                 val celebsCount: Int = 6,
                 val teams: List<Team> = emptyList(),
                 val players: List<Player> = emptyList(),
                 val state: GameState = GameState.Empty()
) {
    val currentPlayer: Player?
        get() = this.state.gameInfo.currentPlayer
    val currentRound: Int
        get() = this.state.gameInfo.round
}

data class GameInfo(
//    val round: Round = Round.Speaking,
    val round: Int = 1,
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val currentPlayer: Player? = null
)

sealed class GameState(open val gameInfo: GameInfo) {
    data class Empty(override val gameInfo: GameInfo = GameInfo()) : GameState(gameInfo)

    data class Ready(override val gameInfo: GameInfo) : GameState(gameInfo)

    data class Started(override val gameInfo: GameInfo) : GameState(gameInfo)

    data class Finished(override val gameInfo: GameInfo) : GameState(gameInfo)
}
