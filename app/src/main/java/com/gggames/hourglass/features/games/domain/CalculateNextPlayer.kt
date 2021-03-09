package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.TeamWithPlayers
import javax.inject.Inject

class CalculateNextPlayer @Inject constructor(){
    operator fun invoke(teams: List<TeamWithPlayers>, lastTeamName: String?): Player? {
        return if (lastTeamName == null) {
            if (teams.first().players.isNotEmpty()) {
                teams.first().players.random()
            } else {
                null
            }
        } else {
            val lastTeamIdx = teams.indexOfFirst { it.name == lastTeamName }

            for (i in 1..teams.size) {
                val nextTeam = teams[(lastTeamIdx + i) % teams.size]
                if (nextTeam.players.isEmpty()) continue
                val lastPlayerIdx = nextTeam.players.indexOfFirst { it.id == nextTeam.lastPlayerId }
                val nextPlayer = nextTeam.players[(lastPlayerIdx + 1) % nextTeam.players.size]
                return nextPlayer
            }
            return null
        }
    }
}