package com.gggames.hourglass.model.remote

data class CardRaw(
    val id: String,
    val name: String,
    val player: String,
    val used: Boolean = false,
    val index: Int = 0,
    val videoUrl1: String? = null,
    val videoUrl2: String? = null,
    val videoUrl3: String? = null,
    val videoUrlFull: String? = null
) {
    constructor() : this (
        EMPTY_VALUE,
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}
