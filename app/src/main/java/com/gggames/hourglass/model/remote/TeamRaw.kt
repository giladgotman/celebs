package com.gggames.hourglass.model.remote

data class TeamRaw(
    val name: String = "",
    val score: Int = 0,
    val lastPlayerId: String? = null
)
