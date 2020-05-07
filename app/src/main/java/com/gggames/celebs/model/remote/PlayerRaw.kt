package com.gggames.celebs.model.remote

import com.gggames.celebs.model.remote.EMPTY_VALUE

data class PlayerRaw (
    val id: String,
    val name: String,
    val team: String? = null
){
    constructor() : this(
        EMPTY_VALUE,
        EMPTY_VALUE
    )
}
