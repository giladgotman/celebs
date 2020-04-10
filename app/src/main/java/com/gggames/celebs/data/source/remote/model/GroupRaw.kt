package com.gggames.celebs.data.source.remote.model

data class GroupRaw (
    val name: String ="",
    val players: List<PlayerRaw> = emptyList()
){
    constructor() : this(EMPTY_VALUE, emptyList())
}


