package com.gggames.celebs.presentation.endgame

import android.widget.TextView
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Team
import com.google.android.exoplayer2.ui.PlayerView
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
        object StartKonffeti : Trigger()
        data class ShowVideoAndKonffeti(val card: Card, val playerView: PlayerView, val giftText: TextView) : Trigger()
    }

    sealed class Result {
        data class GameAndCardsResult(val game: Game, val cards: List<Card>) : Result()
        object GameCleared : Result()
        object StartKonffetiResult : Result()
        data class CardPressedResult(val card: Card, val playerView: PlayerView, val giftText: TextView) : Result()
    }

    sealed class UiEvent {
        object PressedFinish : UiEvent()
        data class PressedCard(val card: Card, val playerView: PlayerView, val giftText: TextView) :
            UiEvent()
    }

    interface Presenter {
        fun bind(events: Observable<UiEvent>, gameId: String)
        fun unBind()
        val states: Observable<State>
        val triggers: Observable<Trigger>
    }
}
