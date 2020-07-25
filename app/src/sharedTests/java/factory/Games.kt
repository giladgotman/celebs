package com.gggames.celebs.common.factory

import com.gggames.celebs.model.*

fun createGame(
    id: String = "id",
    name: String = "name",
    createdAt: Long = 0,
    password: String? = null,
    celebsCount: Int = 6,
    teams: List<Team> = emptyList(),
    state: GameState? = null,
    gameInfo: GameInfo = GameInfo(),
    host: Player = Player("$id.player", "$id.name"),
    type: GameType = GameType.Normal
) = Game(
    id,
    name,
    createdAt,
    password,
    celebsCount,
    teams,
    state,
    gameInfo,
    host,
    type
)
