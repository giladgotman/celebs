package com.gggames.hourglass.presentation.gameon

import com.gggames.hourglass.model.*

interface GameScreenContract {

    sealed class UiEvent {
        data class StartStopClick(val buttonState: ButtonState, val time: Long?) : UiEvent()
        data class CorrectClick(val time: Long) : UiEvent()
        object EndTurnClick : UiEvent()
        object CardsAmountClick : UiEvent()
        data class RoundClick(val time: Long) : UiEvent()
        object TimerEnd : UiEvent()
        object UserApprovedQuitGame : UiEvent()
        object UserApprovedEndTurn : UiEvent()
        object OnBackPressed : UiEvent()
        object OnSwitchTeamPressed : UiEvent()
        object RoundOverDialogDismissed : UiEvent()

        sealed class MainUiEvent : UiEvent()

        override fun toString(): String {
            return this.javaClass.simpleName
        }
    }

    enum class ButtonState {
        Stopped,
        Running,
        Paused,
        Finished
    }

    data class State(
        val screenTitle: String = "",
        val totalCardsInGame: Int = 0,
        val cardsInDeck: Int = 0,
        val currentCard: Card? = null,
        val currentPlayer: Player? = null,
        val revealCurrentCard: Boolean = false,
        val teamsWithScore: List<Team> = emptyList(),
        val teamsWithPlayers: List<TeamWithPlayers> = emptyList(),
        val round: Round = Round(),
        val previousRoundName: String = "1",
        val playButtonState: PlayButtonState = PlayButtonState(),
        val correctButtonEnabled: Boolean = false, // also affected by inProgress
        val inProgress: Boolean = false,
        val helpButtonEnabled: Boolean = false,
        val isTimerRunning: Boolean = false, // also affected by inProgress
        val time: Long? = null,
        val resetTime: Boolean = false,
        val showEndOfTurn: Boolean = false,
        val showEndOfRound: Boolean = false,
        val showGameOver: Boolean = false,
        val lastPlayer: Player? = null,
        val nextPlayer: Player? = null,
        val cardsFoundInTurn: List<Card> = emptyList(),
        val showLeaveGameConfirmation: Boolean = false,
        val showEndTurnConfirmation: Boolean = false,
        val navigateToGames: Boolean = false,
        val navigateToTeams: Boolean = false,
        val useLocalTimer: Boolean = false,
        val showRoundInstructions: Boolean = false,
        val isEndTurnEnabled: Boolean = false,
        val isCardsAmountEnabled: Boolean = false
    ) {
        companion object {
            val initialState = State()
        }

        override fun toString() =
            """
                screenTitle:                    $screenTitle
                totalCardsInGame:               $totalCardsInGame
                cardsInDeck:                    $cardsInDeck
                currentCard:                    $currentCard
                currentPlayer:                  $currentPlayer
                revealCurrentCard:              $revealCurrentCard
                teamsWithPlayers:               $teamsWithPlayers
                teamsWithScore:                 ${teamsWithScore.map { Pair(it.name, it.score) }}
                previousRoundName:              $previousRoundName
                round:                          $round
                isTimerRunning:                 $isTimerRunning
                time:                           $time
                playButtonState                 $playButtonState
                correctButtonEnabled            $correctButtonEnabled
                inProgress                      $inProgress
                helpButtonEnabled               $helpButtonEnabled
                resetTime                       $resetTime
                showEndOfTurn                   $showEndOfTurn
                showEndOfRound                  $showEndOfRound
                showGameOver                    $showGameOver
                lastPlayer                      ${lastPlayer?.name}
                nextPlayer                      ${nextPlayer?.name}
                cardsFoundInTurnSize            ${cardsFoundInTurn.size}
                showLeaveGameConfirmation       $showLeaveGameConfirmation
                showEndTurnConfirmation         $showEndTurnConfirmation
                navigateToGames                 $navigateToGames
                navigateToTeams                 $navigateToTeams
                useLocalTimer                   $useLocalTimer
                showRoundInstructions           $showRoundInstructions
                isEndTurnEnabled                $isEndTurnEnabled
                isCardsAmountEnabled            $isCardsAmountEnabled
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
        data class CombinedGameUpdate(
            val game: Game,
            val players: List<Player>,
            val cards: List<Card>
        ) : Result()

        sealed class HandleNextCardResult : Result() {
            object InProgress : HandleNextCardResult()
            data class NewCard(val newCard: Card, val time: Long?) : HandleNextCardResult()
            data class RoundOver(val round: Round, val newRound: Round, val time: Long?) : HandleNextCardResult()
            object GameOver : HandleNextCardResult()
        }

        sealed class SetGameResult(open val label: String?) : Result() {
            data class InProgress(override val label: String?) : SetGameResult(label) {
                override fun toString(): String {
                    return super.toString()
                }
            }

            data class Done(override val label: String?) : SetGameResult(label) {
                override fun toString(): String {
                    return super.toString()
                }
            }

            override fun toString(): String {
                return "SetGameResult.${this.javaClass.simpleName}.$label"
            }
        }


        sealed class EndTurnPressedResult : Result() {
            data class ShowLeaveGameConfirmation(val showDialog: Boolean) : EndTurnPressedResult()
        }


        sealed class BackPressedResult : Result() {
            data class ShowLeaveGameConfirmation(val showDialog: Boolean) : BackPressedResult()
            data class NavigateToGames(val navigate: Boolean) : BackPressedResult()
        }

        data class NavigateToSelectTeam(val navigate: Boolean) : Result()

        object RoundOverDialogDismissedResult : Result()
        object StartedGameResult : Result()

        data class ShowRoundInstructionsResult(val show: Boolean) : Result()

        data class ShowAllCardsResult(val cards: List<Card>) : Result()

        object NoOp : Result()
    }

    sealed class Trigger {
        data class ShowAllCards(val cards: List<Card>) : Trigger()
        object StartTimer : Trigger()
    }
}
