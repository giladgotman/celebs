package com.gggames.celebs.presentation

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.players.PlayersRepository
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

    private lateinit var playersRepository: PlayersRepository

    private lateinit var firebaseCardsDataSource: FirebaseCardsDataSource
    private lateinit var firebasePlayersDataSource: FirebasePlayersDataSource


    private var cardDeck = mutableListOf<Card>()

    private val schedulerProvider = SchedulerProvider()

    private val disposables = CompositeDisposable()
    private lateinit var view: GameView

    fun bind(view: GameView) {
        this.view = view
        val gameId = GameFlow.currentGame!!.id
        firebaseCardsDataSource = FirebaseCardsDataSource(gameId, FirebaseFirestore.getInstance())
        cardsRepository = CardsRepositoryImpl(firebaseCardsDataSource)

        firebasePlayersDataSource = FirebasePlayersDataSource(FirebaseFirestore.getInstance())
        playersRepository = PlayersRepositoryImpl(firebasePlayersDataSource)

        playersObservable = ObservePlayers(playersRepository, schedulerProvider)
        cardsObservable = ObserveAllCards(cardsRepository, schedulerProvider)

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
            .compose(schedulerProvider.applyDefault())
            .distinctUntilChanged()
            .subscribe({cards->
                cardDeck = cards.toMutableList()
                view.updateCards(cards.filter { !it.used })
            }, {
                Timber.e(it, "error while observing cards")
            }).let {
                disposables.add(it)
            }
    }

    fun onPickNextCard() {
        val notUsedCards = cardDeck.filter { !it.used }
        val card = if (notUsedCards.isNotEmpty()) notUsedCards.random().copy(used = true) else null
        card?.let {
            cardsRepository.updateCard(card)
                .subscribe({
                    view.updateCard(card)
                }, {
                    Timber.e(it, "error while update card")
                }).let {
                    disposables.add(it)
                }
        } ?: Timber.w("no un used cards left!")
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