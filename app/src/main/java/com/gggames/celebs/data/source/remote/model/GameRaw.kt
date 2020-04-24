package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.Round
import com.google.firebase.Timestamp

data class GameRaw (
    val id: String,
    val name: String,
    val createdAt: Timestamp,
    val celebsCount: Long = 6,
    val teams: List<TeamRaw>,
    val rounds: List<RoundRaw>,
    val players: List<PlayerRaw> = emptyList(),
    val state: GameStateRaw = GameStateRaw()
) {
    constructor() : this(
        EMPTY_VALUE,
        EMPTY_VALUE,
        Timestamp.now(),
        6,
        emptyList(),
        emptyList()
    )
}

data class GameInfoRaw(
    val round: RoundRaw = Round.Speaking.toRaw(),
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val currentPlayer: PlayerRaw? = null
)

data class GameStateRaw (
    val state: String,
    val myCards: List<CardRaw> = emptyList(),
    val otherCardsCount: Map<String, Int> = emptyMap(),
    val gameInfo: GameInfoRaw = GameInfoRaw()
) {
    constructor() : this("created")
}

const val EMPTY_VALUE = "EMPTY_VALUE"