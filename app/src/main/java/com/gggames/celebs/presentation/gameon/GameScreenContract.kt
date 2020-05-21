package com.gggames.celebs.presentation.gameon

interface GameScreenContract {


    sealed class UiEvent {
        data class StartStopClick(val buttonState: ButtonState) : UiEvent()
        data class CorrectClick(val time: Long) : UiEvent()
        object EndTurnClick : UiEvent()
        object CardsAmountClick : UiEvent()
        object RoundClick : UiEvent()
        object TimerEnd : UiEvent()
//        data class UpdateTime(val time: Long) : UiEvent()
    }

//    sealed class ButtonState {
//        object Stopped : ButtonState()
//        object Running : ButtonState()
//        object Paused : ButtonState()
//    }

    sealed class ButtonState(time: Long?) {
        object Stopped : ButtonState(null)
        data class Running(val time: Long) : ButtonState(time)
        data class Paused(val time: Long) : ButtonState(time)
    }

}