package com.gggames.celebs.model.remote

import com.gggames.celebs.model.Round
import com.gggames.celebs.model.Turn
import com.google.firebase.Timestamp

data class GameRaw (
    val id: String,
    val name: String,
    val createdAt: Timestamp,
    val celebsCount: Long = 6,
    val teams: List<TeamRaw>,
    val state: String? = null,
    val gameInfo: GameInfoRaw = GameInfoRaw()
) {
    constructor() : this(
        EMPTY_VALUE,
        EMPTY_VALUE,
        Timestamp.now(),
        6,
        emptyList()
    )
}

data class GameInfoRaw(
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val round: RoundRaw = RoundRaw()
)

data class RoundRaw(
    val roundState: String = Round().state.toRaw(),
    val roundNumber: Int = 1,
    val turn: TurnRaw = TurnRaw()
)

data class TurnRaw(
    val state: String = Turn().state.toRaw(),
    val player: PlayerRaw? = null,
    val time: Long? = null
)

const val EMPTY_VALUE = "EMPTY_VALUE"