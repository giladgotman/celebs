package com.gggames.celebs.data.source.remote.model

data class CardRaw(
    val name: String,
    val player: String
) {
    constructor() : this (EMPTY_VALUE, EMPTY_VALUE)
}