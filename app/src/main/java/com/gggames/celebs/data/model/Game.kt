package com.gggames.celebs.data.model

data class Game (val id: String = "",
                 val name: String = "",
                 val createdAt: Long = 0,
                 val celebsCount: Int = 0,
                 val groups: List<Group> = emptyList()
)