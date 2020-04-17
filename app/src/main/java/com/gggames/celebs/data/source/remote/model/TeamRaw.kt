package com.gggames.celebs.data.source.remote.model

data class TeamRaw (
    val name: String ="",
    val players: List<PlayerRaw> = emptyList()
){
    constructor() : this(EMPTY_VALUE, emptyList())
}


