package com.gggames.celebs.model.remote

data class CardRaw(
    val id: String? = null,
    val name: String,
    val player: String,
    val used: Boolean = false
) {
    constructor() : this (null,
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}