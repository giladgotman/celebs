package com.gggames.celebs.model

sealed class User {
    data class Guest(val id: String, val name: String): User()
    data class LoggedIn(val id: String, val name: String, val games: List<Game>): User()
}