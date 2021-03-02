package com.gggames.hourglass.model.remote

import com.gggames.hourglass.model.GameType
import com.gggames.hourglass.model.Round
import com.gggames.hourglass.model.Turn
import com.google.firebase.Timestamp

data class GameRaw(
    val id: String,
    val name: String,
    val createdAt: Timestamp,
    val password: String?,
    val celebsCount: Long = 6,
    val teams: List<TeamRaw> = emptyList(),
    val state: String? = null,
    val gameInfo: GameInfoRaw = GameInfoRaw(),
    val host: PlayerRaw = PlayerRaw(),
    val type: String = GameType.Normal.name
) {
    constructor() : this(
        id = EMPTY_VALUE,
        name = EMPTY_VALUE,
        createdAt = Timestamp.now(),
        password = EMPTY_VALUE,
        celebsCount = 6
    )
}

data class GameInfoRaw(
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
    val nextPlayer: PlayerRaw? = null,
    val time: Long? = null,
    val cardsFound: List<String> = emptyList(),
    val lastFoundCard: CardRaw? = null,
    val currentCard: CardRaw? = null
)

const val EMPTY_VALUE = "EMPTY_VALUE"
