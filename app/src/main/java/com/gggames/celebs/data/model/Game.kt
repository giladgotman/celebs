package com.gggames.celebs.data.model

data class Game (val id: String, val name: String, val createdAt: Long, val celebsCount: Int, val groups: List<Group>)