package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Player
import com.gggames.celebs.model.Round

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
        val currentCard: Card? = null
    ) {
        companion object {
            val initialState = State()
        }

        override fun toString(): String {
            return """
                cardsInDeck: $cardsInDeck
                currentCard: $currentCard
                """.trimIndent()
        }
    }

    sealed class Result {
        data class GameUpdate(val game: Game): Result()
        data class PlayersUpdate(val players: List<Player>): Result()
        data class CardsUpdate(val cards: List<Card>): Result()

        sealed class PickNextCardResult : Result() {
            data class Found(val card: Card) : PickNextCardResult()
            data class NoCardsLeft(val round: Round, val time: Long?) : PickNextCardResult()
        }

        data class CardsUpdateResult(val currentCard: Card? = null) : Result()

        object NoOp : Result()
    }
}
