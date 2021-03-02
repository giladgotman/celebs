package com.gggames.hourglass.model

import android.os.Parcelable
import com.gggames.hourglass.model.TurnState.Idle
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

data class Game(
    val id: String,
    val name: String,
    val createdAt: Long,
    val password: String? = null,
    val celebsCount: Int = 6,
    val teams: List<Team> = emptyList(),
    val state: GameState? = null,
    val gameInfo: GameInfo = GameInfo(),
    val host: Player,
    val type: GameType
) {
    val currentPlayer: Player?
        get() = this.gameInfo.round.turn.player
    val currentRound: Int
        get() = this.gameInfo.round.roundNumber
    val round = this.gameInfo.round
    val turn = this.gameInfo.round.turn

    val winningTeam: Team? get() = this.teams.maxBy { it.score }
}

enum class GameType {
    Normal,
    Gift
}

data class GameInfo(
    val totalCards: Int = 0,
    val cardsInDeck: Int = 0,
    val round: Round = Round()
)

@Parcelize
data class Round(
    val state: RoundState = RoundState.Ready,
    val roundNumber: Int = 1,
    val turn: Turn = Turn()
): Parcelable


fun roundIdToName(roundId: Int): String {
    return when (roundId) {
        1 -> "Describe"
        2 -> "One Word"
        3 -> "Charades"
        else -> "Unknown"
    }
}

enum class RoundState {
    Ready,
    Started,
    Ended,
    New;

    companion object {
        fun fromName(name: String?): RoundState =
            when (name) {
                "Ready" -> Ready
                "Started" -> Started
                "Ended" -> Ended
                "New" -> New
                else -> {
                    Timber.w("Unknown round state name: $name , setting state to Ready")
                    Ready
                }
            }
    }
}

@Parcelize
data class Turn(
    val state: TurnState = Idle,
    val player: Player? = null,
    val nextPlayer: Player? = null,
    val time: Long? = null,
    val cardsFound: List<String> = emptyList(),
    val lastFoundCard: Card? = null,
    val currentCard: Card? = null
): Parcelable

enum class TurnState {
    Idle,
    Over,
    Running,
    Paused;

    companion object {
        fun fromName(name: String?): TurnState =
            when (name) {
                "Idle" -> Idle
                "Over" -> Over
                "Running" -> Running
                "Paused" -> Paused
                else -> {
                    Timber.w("Unknown turn state name: $name , setting state to Idle")
                    Idle
                }
            }
    }
}

fun TurnState.isTurnOn(): Boolean = this == TurnState.Running || this == TurnState.Paused

fun TurnState.toPlayButtonState() =
    when (this) {
        TurnState.Idle -> GameScreenContract.ButtonState.Stopped
        TurnState.Over -> GameScreenContract.ButtonState.Stopped
        TurnState.Running -> GameScreenContract.ButtonState.Running
        TurnState.Paused -> GameScreenContract.ButtonState.Paused
    }

enum class GameState {
    Created,
    Started,
    Finished;

    companion object {
        fun fromName(name: String?): GameState? =
            when (name) {
                "Created" -> Created
                "Started" -> Started
                "Finished" -> Finished
                null -> null
                else -> {
                    Timber.w("Unknown game state name: $name , setting state to Created")
                    Created
                }
            }
    }
}

fun Game.setGameState(state: GameState) = this.copy(state = state)

fun Game.setRoundState(state: RoundState) =
    this.copy(gameInfo = this.gameInfo.copy(round = this.gameInfo.round.copy(state = state)))

fun Game.setRoundNumber(roundNumber: Int) =
    this.copy(gameInfo = this.gameInfo.copy(round = this.gameInfo.round.copy(roundNumber = roundNumber)))

fun Game.setTurnState(state: TurnState) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(state = state)
        )
    )
)

fun Game.setCurrentCard(card: Card?) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(currentCard = card)
        )
    )
)

fun Game.addCardsFoundInTurn(card: Card) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(cardsFound = this.turn.cardsFound + card.id)
        )
    )
)

fun Game.resetCardsFoundInTurn() = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(cardsFound = emptyList())
        )
    )
)

fun Game.setTurnPlayer(player: Player?) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(player = player)
        )
    )
)

fun Game.setTurnNextPlayer(player: Player?) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(nextPlayer = player)
        )
    )
)

fun Game.setTeamLastPlayerId(player: Player?) = this.copy(
    teams = this.teams.map { team ->
        if (team.name == player?.team) {
            team.copy(lastPlayerId = player.id)
        } else team
    }
)

fun Game.setTurnTime(time: Long?) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(time = time)
        )
    )
)

fun Game.setTurnLastCards(cardsIds: List<String>) = this.copy(
    gameInfo = this.gameInfo.copy(
        round = this.gameInfo.round.copy(
            turn = this.gameInfo.round.turn.copy(cardsFound = cardsIds)
        )
    )
)

fun Game.increaseScore(teamName: String): Game {
    val teamIndex = this.teams.indexOfFirst { it.name == teamName }
    if (teamIndex != -1) {
        val currScore = this.teams[teamIndex].score
        val currTeam = this.teams[teamIndex]
        val mutableTeams = this.teams.toMutableList()
        mutableTeams[teamIndex] = currTeam.copy(score = currScore + 1)
        return this.copy(teams = mutableTeams)
    } else {
        throw java.lang.IllegalArgumentException("Can't find teamName: $teamName")
    }
}
