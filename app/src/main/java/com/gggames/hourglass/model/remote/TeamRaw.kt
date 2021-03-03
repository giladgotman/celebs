package com.gggames.hourglass.model.remote

data class TeamRaw(
    val name: String = "",
    val playerIds: List<String> = emptyList(),
    val score: Int = 0,
    val lastPlayerId: String? = null
)
