package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.Team
import javax.inject.Inject

class CalculateNextPlayer @Inject constructor() {
    operator fun invoke(teams: List<Team>, lastTeamName: String?): Player? =
        if (teams.isEmpty()) null
        else if (lastTeamName == null) {
            if (teams.first().players.isNotEmpty()) {
                teams.first().players.random()
            } else {
                null
            }
        } else {
            val lastTeamIdx = teams.indexOfFirst { it.name == lastTeamName }
            val nextTeam = teams[(lastTeamIdx + 1) % teams.size]
            if (nextTeam.players.isNotEmpty()) {
                val lastPlayerIdx = nextTeam.players.indexOfFirst { it.id == nextTeam.lastPlayerId }
                val nextPlayer = nextTeam.players[(lastPlayerIdx + 1) % nextTeam.players.size]
                nextPlayer
            } else {
                null
            }
        }
}