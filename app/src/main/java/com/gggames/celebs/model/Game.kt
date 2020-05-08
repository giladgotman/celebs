package com.gggames.celebs.model

import com.gggames.celebs.model.TurnState.Stopped

data class Game (val id: String,
                 val name: String,
                 val createdAt: Long,
                 val celebsCount: Int = 6,
                 val teams: List<Team> = emptyList(),
                 val state: GameState? = null,
                 val gameInfo: GameInfo = GameInfo()
) {
    val currentPlayer: Player?
        get() = this.gameInfo.round.turn.player
    val currentRound: Int
        get() = this.gameInfo.round.roundNumber
}

data class GameInfo(
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val round: Round = Round()
)

data class Round(val state: RoundState = RoundState.Ready, val roundNumber: Int = 1, val turn: Turn = Turn())

enum class RoundState {
    Ready,
    Ended;

    companion object {
        fun fromName(name: String?): RoundState =
            when (name) {
                "Ready" -> Ready
                "Ended" -> Ended
                else -> throw IllegalArgumentException("Unknown round state name: $name")
            }
    }
}

data class Turn(val state: TurnState = Stopped, val player: Player? = null, val time: String? = null)


enum class TurnState {
    Stopped,
    Running,
    Paused;

    companion object {
        fun fromName(name: String?): TurnState =
            when (name) {
                "Stopped" -> Stopped
                "Running" -> Running
                "Paused" -> Paused
                else -> throw IllegalArgumentException("Unknown turn state name: $name")
            }
    }
}
enum class GameState {
    Created,
    Started,
    Finished;

    companion object {
        fun fromName(name: String?): GameState? =
            when (name) {
                "Created" -> Created
                "Started" -> Started
                "Finished" -> Finished
                null -> null
                else -> throw IllegalArgumentException("Unknown game state name: $name")
            }
    }

}
