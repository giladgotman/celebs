package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.remote.createTeam
import com.gggames.celebs.features.players.data.remote.createPlayer
import com.gggames.celebs.model.Team
import com.google.common.truth.Truth
import org.junit.Test

class CalculateNextPlayerTest {


    val tested = CalculateNextPlayer()

    val teams = listOf<Team>(
        createTeam(
            "team1",
            players = listOf(
                createPlayer("player1", "player1"),
                createPlayer("player2", "player2")
            ),
            lastPlayerId = "player1"
        ),
        createTeam(
            "team2",
            players = listOf(
                createPlayer("player3", "player3"),
                createPlayer("player4", "player4")
            )
        )
    )

    @Test
    fun `Given player1 is lastPlayer of team1 And lastTeam is team2 Then result is player2`() {
        val nextPlayer = tested(teams, "team2")
        Truth.assertThat(nextPlayer?.id).isEqualTo("player2")
    }
}