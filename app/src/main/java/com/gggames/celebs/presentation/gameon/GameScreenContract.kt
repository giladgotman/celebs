package com.gggames.celebs.presentation.gameon

interface GameScreenContract {

    sealed class UiEvent {
        data class StartStopClick(val buttonState: ButtonState, val time: Long?) : UiEvent()
        data class CorrectClick(val time: Long) : UiEvent()
        object EndTurnClick : UiEvent()
        object CardsAmountClick : UiEvent()
        object RoundClick : UiEvent()
        object TimerEnd : UiEvent()
    }

    enum class ButtonState {
        Stopped,
        Running,
        Paused
    }

}