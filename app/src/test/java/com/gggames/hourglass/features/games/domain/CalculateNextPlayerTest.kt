package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.remote.createTeamWithPlayers
import com.gggames.hourglass.features.players.data.remote.createPlayer
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.TeamWithPlayers
import com.google.common.truth.Truth
import org.junit.Test

class CalculateNextPlayerTest {


    val tested = CalculateNextPlayer()

    private var teams: List<TeamWithPlayers> = emptyList()


    @Test
    fun `Given player1_1 is lastPlayer of team1 And lastTeam is team2 Then result is player1_2`() {
        givenTeamsAre(
            listOf(
                createTeamWithLastPlayer(1, players = createDefaultPlayers(1), lastPlayer = "player1.1"),
                createTeamWithLastPlayer(2, players = createDefaultPlayers(2), lastPlayer = null)
            )
        )
        val lastTeam = "team2"
        val nextPlayer = tested(teams, lastTeam)
        val expected = "player1.2"
        Truth.assertThat(nextPlayer?.id).isEqualTo(expected)
    }

    @Test
    fun `Given team3 has no players and player1 is lastPlayer of team1 And lastTeam is team2 Then result is player2`() {
        givenTeamsAre(
            listOf(
                createTeamWithLastPlayer(1, players = createDefaultPlayers(1), lastPlayer = "player1.1"),
                createTeamWithLastPlayer(2, players = createDefaultPlayers(2), lastPlayer = "player2.1"),
                createNoPlayersTeamWithLastPlayer(3)
            )
        )
        val lastTeam = "team2"
        val nextPlayer = tested(teams, lastTeam)
        val expected = "player1.2"
        Truth.assertThat(nextPlayer?.id).isEqualTo(expected)
    }

    @Test
    fun `Good weather flow 3 teams`() {
        givenTeamsAre(
            listOf(
                createTeamWithLastPlayer(1, players = createDefaultPlayers(1), lastPlayer = "player1.1"),
                createTeamWithLastPlayer(2, players = createDefaultPlayers(2), lastPlayer = "player2.1"),
                createTeamWithLastPlayer(3, players = createDefaultPlayers(3), lastPlayer = null)
            )
        )
        var lastTeam = "team2"
        var nextPlayer = tested(teams, lastTeam)
        var expected = "player3.1"
        Truth.assertThat(nextPlayer?.id).isEqualTo(expected)

        lastTeam = "team3"
        nextPlayer = tested(teams, lastTeam)
        expected = "player1.2"
        Truth.assertThat(nextPlayer?.id).isEqualTo(expected)

        lastTeam = "team1"
        nextPlayer = tested(teams, lastTeam)
        expected = "player2.2"
        Truth.assertThat(nextPlayer?.id).isEqualTo(expected)
    }


    private fun givenTeamsAre(teamWithPlayers: List<TeamWithPlayers>) {
        teams = teamWithPlayers
    }

    private fun createTeamWithLastPlayer(teamIndex: Int, players: List<Player>, lastPlayer: String? = null) =
        createTeamWithPlayers(
            name = "team$teamIndex",
            players = players,
            lastPlayerId = lastPlayer
        )

    private fun createDefaultPlayers(teamIndex: Int) = createPlayerList(teamIndex, 2)

    private fun createPlayerList(teamIndex: Int, amount: Int): List<Player> {
        val list = mutableListOf<Player>()
        for (i in 1..amount) {
            list.add(createPlayer("player$teamIndex.$i", "player$teamIndex.$i"))

        }
        return list
    }

    private fun createNoPlayersTeamWithLastPlayer(teamIndex: Int) =
        createTeamWithPlayers(
            name = "team$teamIndex",
            players = emptyList()
        )
}