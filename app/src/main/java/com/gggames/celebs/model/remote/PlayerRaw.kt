package com.gggames.celebs.model.remote

data class PlayerRaw(
    val id: String,
    val name: String,
    val team: String? = null,
    val games: List<String> = emptyList()
) {
    constructor() : this(
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}
