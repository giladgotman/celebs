package com.gggames.celebs.presentation.gameon

import android.graphics.drawable.Drawable
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


    data class GameUiState(
        val correctBtn: SimpleView = SimpleView("v", isVisible = true, isEnabled = false)
    )
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

        sealed class IncreaseScoreResult: Result() {
            object Loading : IncreaseScoreResult()
            object Completed: IncreaseScoreResult()
            object Error: IncreaseScoreResult()
        }
    }

    data class SimpleView(
        val text: String = "",
        val isVisible: Boolean = false,
        val isEnabled: Boolean = false,
        val image: Drawable? = null
    )

}


