package com.gggames.celebs.presentation

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.players.PlayersRepositoryImpl
import com.gggames.celebs.data.source.remote.FirebaseCardsDataSource
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.cards.ObserveAllCards
import com.gggames.celebs.domain.players.ObservePlayers
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class GamePresenter {

    private lateinit var playersObservable: ObservePlayers

    private lateinit var cardsObservable: ObserveAllCards

    private lateinit var cardsRepository: CardsRepository

    private val disposables = CompositeDisposable()
    lateinit var view: GameView

    fun bind(view: GameView) {
        this.view = view
        val gameId = GameFlow.currentGame!!.id
        cardsRepository = CardsRepositoryImpl(
            FirebaseCardsDataSource(
                gameId,
                FirebaseFirestore.getInstance()
            )
        )

        playersObservable = ObservePlayers(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        cardsObservable = ObserveAllCards(
            cardsRepository,
            SchedulerProvider()
        )

        playersObservable(gameId)
            .distinctUntilChanged()
            .subscribe({list->
                view.updateTeams(list)
            }, {
                Timber.e(it, "error while observing players")
            }).let {
                disposables.add(it)
            }

        cardsObservable()
            .distinctUntilChanged()
            .subscribe({cards->
                view.updateCards(cards)
            }, {
                Timber.e(it, "error while observing cards")
            }).let {
                disposables.add(it)
            }
    }

    fun onPickNextCard() {
        val card = cardsRepository.pickCard()
        view.updateCard(card)
    }

    fun onPlayerStarted() {
        onPickNextCard()
    }

    fun unBind() {
        disposables.clear()
    }


    interface GameView{
        fun updateCards(cards: List<Card>)
        fun updateTeams(list: List<Player>)
        fun updateCard(card: Card)
    }
}