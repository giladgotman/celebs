package com.gggames.celebs.data.model

data class Player (val name: String)

data class PlayersWithCards(private val player: Player, private val createdCards: Int)
