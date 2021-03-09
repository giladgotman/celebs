package com.gggames.hourglass.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val name: String,
    val playerIds: List<String> = emptyList(),
    val score: Int = 0,
    val lastPlayerId: String? = null
) : Parcelable


data class TeamWithPlayers(
    val name: String,
    val players: List<Player> = emptyList(),
    val score: Int = 0,
    val lastPlayerId: String? = null
)


fun TeamWithPlayers.toTeam() = Team(name, players.map { it.id }, score, lastPlayerId)
fun Team.toTeamWithPlayers(players: List<Player>) = TeamWithPlayers(name, players, score, lastPlayerId)