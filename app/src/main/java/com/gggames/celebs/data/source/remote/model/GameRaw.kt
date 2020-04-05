package com.gggames.celebs.data.source.remote.model

import com.google.firebase.Timestamp

data class GameRaw (val id: String,
                 val name: String,
                 val createdAt: Timestamp,
                 val celebsCount: Long = 6,
                 val groups: List<GroupRaw> = emptyList(),
                 val rounds: List<RoundRaw>,
                 val state: GameStateRaw
)

data class GameInfoRaw(
    val round: RoundRaw,
    val score: Map<GroupRaw, Int>,
    val totalCards: Int,
    val cardsInDeck: Int,
    val currentPlayer: PlayerRaw
)

data class GameStateRaw (
    val state: String,
    val myCards: List<CardRaw>,
    val otherCardsCount: Map<PlayerRaw, Int>,
    val gameInfo: GameInfoRaw? = null
)
