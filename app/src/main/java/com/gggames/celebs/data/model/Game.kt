package com.gggames.celebs.data.model

data class Game (val id: String,
                 val name: String,
                 val createdAt: Long,
                 val celebsCount: Int = 6,
                 val teams: List<Team> = emptyList(),
                 val state: GameStateE? = null,
                 val gameInfo: GameInfo = GameInfo()
) {
    val currentPlayer: Player?
        get() = this.gameInfo.currentPlayer
    val currentRound: Int
        get() = this.gameInfo.round
}

data class GameInfo(
    val round: Int = 1,
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val currentPlayer: Player? = null
)

enum class GameStateE {
    Created,
    Started,
    Finished;

    companion object {
        fun fromName(name: String?): GameStateE? =
            when (name) {
                "Created" -> Created
                "Started" -> Started
                "Finished" -> Finished
                null -> null
                else -> throw IllegalArgumentException("Unknown game state name: $name")
            }
    }

}

//sealed class GameState(open val gameInfo: GameInfo) {
//    data class Empty(override val gameInfo: GameInfo = GameInfo()) : GameState(gameInfo)
//
//    data class Ready(override val gameInfo: GameInfo) : GameState(gameInfo)
//
//    data class Started(override val gameInfo: GameInfo) : GameState(gameInfo)
//
//    data class Finished(override val gameInfo: GameInfo) : GameState(gameInfo)
//}
