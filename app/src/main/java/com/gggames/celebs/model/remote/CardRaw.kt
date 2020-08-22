package com.gggames.celebs.model.remote

data class CardRaw(
    val id: String? = null,
    val name: String,
    val player: String,
    val used: Boolean = false,
    val index: Int = 0,
    val videoUrl1: String? = null,
    val videoUrl2: String? = null,
    val videoUrl3: String? = null,
    val videoUrlFull: String? = null
) {
    constructor() : this (null,
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}
