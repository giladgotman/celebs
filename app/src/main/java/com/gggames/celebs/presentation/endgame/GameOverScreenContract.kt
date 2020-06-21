package com.gggames.celebs.presentation.endgame

import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Team
import io.reactivex.Observable

interface GameOverScreenContract {

    data class State(
        val winningTeam: String = "",
        val teams: List<Team> = emptyList(),
        val cards: List<Card> = emptyList()
    ) {
        companion object {
            val initialValue = State()
        }
    }

    sealed class Trigger {
        object NavigateToGames : Trigger()
    }

    sealed class Result {
        data class GameResult(val game: Game): Result()
        data class CardsResult(val cards: List<Card>)
        object GameCleared : Result()
    }

    sealed class UiEvent{
        object PressedFinish : UiEvent()
    }

    interface Presenter {
        fun bind(events: Observable<UiEvent>, gameId: String)
        fun unBind()
        val states: Observable<State>
        val triggers: Observable<Trigger>
    }

}