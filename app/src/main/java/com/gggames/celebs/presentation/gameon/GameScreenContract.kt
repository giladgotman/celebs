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
        val totalCardsInGame: Int = 0,
        val cardsInDeck: Int = 0,
        val currentCard: Card? = null,
        val teamsWithScore: List<Team> = emptyList(),
        val teamsWithPlayers: List<Team> = emptyList(),
        val round: String = "1",
        val playButtonState: PlayButtonState = PlayButtonState(),
        val correctButtonEnabled: Boolean = false,
        val isTimerRunning: Boolean = false,
        val meActive: Boolean = false,
        val resetTime: Boolean = false,
        val showEndOfTurn: Boolean = false
    ) {
        companion object {
            val initialState = State()
        }

        override fun toString() =
            """
                totalCardsInGame:               $totalCardsInGame
                cardsInDeck:                    $cardsInDeck
                currentCard:                    $currentCard
                teamsWithPlayers:               ${teamsWithPlayers.map { it.players }}
                teamsWithScore:                 ${teamsWithScore.map { Pair(it.name, it.score) }}
                round:                          $round
                isTimerRunning:                 $isTimerRunning
                playButtonState                 $playButtonState
                correctButtonEnabled            $correctButtonEnabled
                resetTime                       $resetTime
                showEndOfTurn                   $showEndOfTurn
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

        sealed class HandleNextCardResult : Result() {
            data class NewCard(val newCard: Card, val time: Long?) : HandleNextCardResult()
            data class RoundOver(val round: Round, val newRound: Round, val time: Long?) : HandleNextCardResult()
            object GameOver : HandleNextCardResult()
        }

        data class CardsUpdateResult(val currentCard: Card? = null) : Result()

        object NoOp : Result()
    }
}
