package com.gggames.celebs.presentation.gameon

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

}


