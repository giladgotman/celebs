package com.gggames.hourglass.model.remote

data class UserRaw(
    val id: String,
    val name: String,
    val games: List<GameRaw> = emptyList(),
    val type: UserType = UserType.Guest

) {
    constructor() : this(
        id = EMPTY_VALUE,
        name = EMPTY_VALUE
    )
}

enum class UserType {
    Guest,
    LoggedIn
}
