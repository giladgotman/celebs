package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.model.*

interface GameScreenContract {

    sealed class UiEvent {
        data class StartStopClick(val buttonState: ButtonState, val time: Long?) : UiEvent()
        data class CorrectClick(val time: Long) : UiEvent()
        object EndTurnClick : UiEvent()
        object CardsAmountClick : UiEvent()
        data class RoundClick(val time: Long) : UiEvent()
        object TimerEnd : UiEvent()
        object UserApprovedQuitGame : UiEvent()
        object OnBackPressed : UiEvent()
        object OnSwitchTeamPressed : UiEvent()

        sealed class MainUiEvent : UiEvent()
    }

    enum class ButtonState {
        Stopped,
        Running,
        Paused,
        Finished
    }

    data class State(
        val cardsInDeck: Int = 0,
        val currentCard: Card? = null,
        val teams: List<Team> = emptyList(),
        val round: String = "1",
        val playButtonState: PlayButtonState = PlayButtonState(),
        val isTimerRunning: Boolean = false,
        val meActive: Boolean = false
    ) {
        companion object {
            val initialState = State()
        }

        override fun toString() =
            '\n' + """
                cardsInDeck:        $cardsInDeck
                currentCard:        $currentCard
                teamsSize:          ${teams.size}
                round:              $round
                isTimerRunning:     $isTimerRunning
                playButtonState     $playButtonState
                """.trimIndent()

    }

    data class PlayButtonState(
        val isEnabled: Boolean = false,
        val state: ButtonState = ButtonState.Stopped
    )

    sealed class Result {
        data class GameUpdate(val game: Game) : Result()
        data class PlayersUpdate(val players: List<Player>) : Result()
        data class CardsUpdate(val cards: List<Card>) : Result()

        sealed class PickNextCardResult : Result() {
            data class Found(val card: Card) : PickNextCardResult()
            data class NoCardsLeft(val round: Round, val time: Long?) : PickNextCardResult()
        }

        data class CardsUpdateResult(val currentCard: Card? = null) : Result()

        object NoOp : Result()
    }
}
