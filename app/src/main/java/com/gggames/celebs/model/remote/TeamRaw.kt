package com.gggames.celebs.model.remote

import com.gggames.celebs.model.remote.EMPTY_VALUE
import com.gggames.celebs.model.remote.PlayerRaw

data class TeamRaw (
    val name: String ="",
    val players: List<PlayerRaw> = emptyList()
){
    constructor() : this(EMPTY_VALUE, emptyList())
}


