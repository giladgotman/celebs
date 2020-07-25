package com.gggames.celebs.model

sealed class User(open val id: String) {
    data class Guest(override val id: String, val name: String) : User(id)
    data class LoggedIn(override val id: String, val name: String, val games: List<Game>) : User(id)
}
