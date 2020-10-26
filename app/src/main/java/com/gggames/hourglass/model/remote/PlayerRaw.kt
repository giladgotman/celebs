package com.gggames.hourglass.model.remote

import com.gggames.hourglass.model.PlayerTurnState

data class PlayerRaw(
    val id: String,
    val name: String,
    val team: String? = null,
    val games: List<String> = emptyList(),
    val playerTurnState: String = PlayerTurnState.Idle.name
) {
    constructor() : this(
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}
