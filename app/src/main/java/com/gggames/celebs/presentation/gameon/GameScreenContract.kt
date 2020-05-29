package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Player

interface GameScreenContract {

    sealed class UiEvent {
        data class StartStopClick(val buttonState: ButtonState, val time: Long?) : UiEvent()
        data class CorrectClick(val time: Long) : UiEvent()
        object EndTurnClick : UiEvent()
        object CardsAmountClick : UiEvent()
        data class RoundClick(val time: Long) : UiEvent()
        object TimerEnd : UiEvent()
        object FinishGameClick : UiEvent()
    }

    enum class ButtonState {
        Stopped,
        Running,
        Paused
    }

    data class TeamsState (
        val teamsList: List<TeamState> = emptyList()
    )

    data class TeamState (
        val name: String = "",
        val players: List<String> = emptyList(),
        val score: Int = 0
    )

    sealed class Result {
        data class GameResult(val game: Game): Result()
        data class PlayersResult(val players: List<Player>): Result()
        data class CardsResult(val cards: List<Card>): Result()
    }

}


