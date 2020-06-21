package com.gggames.celebs.presentation.endgame

import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Team

interface GameOverScreenContract {

    data class State(
        val winningTeam: String,
        val teams: List<Team>,
        val cards: List<Card>
    )

    sealed class Result {
        data class GameResult(val game: Game): Result()
        data class CardsResult(val cards: List<Card>)
    }

    sealed class UiEvent{
        object PressedFinish : UiEvent()
    }

}