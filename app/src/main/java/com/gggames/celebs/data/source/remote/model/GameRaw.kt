package com.gggames.celebs.data.source.remote.model

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
    val round: Int = 1,
    val score: Map<String, Int> = emptyMap(),
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val currentPlayer: PlayerRaw? = null
)

//data class GameStateRawE (
//    val state: String,
//    val gameInfo: GameInfoRaw = GameInfoRaw()
//) {
//    constructor() : this("empty")
//}

const val EMPTY_VALUE = "EMPTY_VALUE"