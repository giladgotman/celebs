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
        object RoundOverDialogDismissed : UiEvent()

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
        val currentPlayer: Player? = null,
        val revealCurrentCard: Boolean = false,
        val teamsWithScore: List<Team> = emptyList(),
        val teamsWithPlayers: List<Team> = emptyList(),
        val round: Round = Round(),
        val previousRoundName: String = "1",
        val playButtonState: PlayButtonState = PlayButtonState(),
        val correctButtonEnabled: Boolean = false, // also affected by inProgress
        val inProgress: Boolean = false,
        val helpButtonEnabled: Boolean = false,
        val isTimerRunning: Boolean = false, // also affected by inProgress
        val meActive: Boolean = false,
        val resetTime: Boolean = false,
        val showEndOfTurn: Boolean = false,
        val showEndOfRound: Boolean = false,
        val showGameOver: Boolean = false,
        val lastPlayer: Player? = null,
        val cardsFoundInTurn: List<Card> = emptyList(),
        val showLeaveGameConfirmation: Boolean = false,
        val navigateToGames: Boolean = false
    ) {
        companion object {
            val initialState = State()
        }

        override fun toString() =
            """
                totalCardsInGame:               $totalCardsInGame
                cardsInDeck:                    $cardsInDeck
                currentCard:                    $currentCard
                currentPlayer:                  $currentPlayer
                revealCurrentCard:              $revealCurrentCard
                teamsWithPlayers:               ${teamsWithPlayers.map { it.players }}
                teamsWithScore:                 ${teamsWithScore.map { Pair(it.name, it.score) }}
                previousRoundName:              $previousRoundName
                round:                          $round
                isTimerRunning:                 $isTimerRunning
                playButtonState                 $playButtonState
                correctButtonEnabled            $correctButtonEnabled
                inProgress                      $inProgress
                helpButtonEnabled               $helpButtonEnabled
                resetTime                       $resetTime
                showEndOfTurn                   $showEndOfTurn
                showEndOfRound                  $showEndOfRound
                showGameOver                    $showGameOver
                lastPlayer                      ${lastPlayer?.name}
                cardsFoundInTurnSize            ${cardsFoundInTurn.size}
                showLeaveGameConfirmation       $showLeaveGameConfirmation
                navigateToGames                 $navigateToGames
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
            object InProgress: HandleNextCardResult()
            data class NewCard(val newCard: Card, val time: Long?) : HandleNextCardResult()
            data class RoundOver(val round: Round, val newRound: Round, val time: Long?) : HandleNextCardResult()
            object GameOver : HandleNextCardResult()
        }

        sealed class BackPressedResult: Result() {
            data class ShowLeaveGameConfirmation(val showDialog: Boolean): BackPressedResult()
            data class NavigateToGames(val navigate: Boolean) : BackPressedResult()
        }

        object RoundOverDialogDismissedResult : Result()

        object NoOp : Result()
    }
}
