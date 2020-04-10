package com.gggames.celebs.data.source.remote.model

data class PlayerRaw (
    val id: String,
    val name: String
){
    constructor() : this(EMPTY_VALUE, EMPTY_VALUE)
}
