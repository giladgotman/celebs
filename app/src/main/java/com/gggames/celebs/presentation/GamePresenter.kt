package com.gggames.celebs.presentation

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.games.GamesRepository
import com.gggames.celebs.data.games.GamesRepositoryImpl
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.model.GameInfo
import com.gggames.celebs.data.model.GameState
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.players.PlayersRepository
import com.gggames.celebs.data.players.PlayersRepositoryImpl
import com.gggames.celebs.data.source.remote.FirebaseCardsDataSource
import com.gggames.celebs.data.source.remote.FirebaseGamesDataSource
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.cards.ObserveAllCards
import com.gggames.celebs.domain.games.AddGame
import com.gggames.celebs.domain.games.ObserveGame
import com.gggames.celebs.domain.players.ObservePlayers
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class GamePresenter {

    private lateinit var playersObservable: ObservePlayers

    private lateinit var cardsObservable: ObserveAllCards

    private lateinit var updateGame: AddGame
    private lateinit var observeGame: ObserveGame

    private lateinit var cardsRepository: CardsRepository

    private lateinit var playersRepository: PlayersRepository
    private lateinit var gamesRepository: GamesRepository

    private lateinit var firebaseCardsDataSource: FirebaseCardsDataSource
    private lateinit var firebasePlayersDataSource: FirebasePlayersDataSource
    private lateinit var firebaseGamesDataSource: FirebaseGamesDataSource

    private var cardDeck = mutableListOf<Card>()

    private var lastCard: Card? = null

    private val schedulerProvider = SchedulerProvider()

    private val disposables = CompositeDisposable()
    private lateinit var view: GameView

    val STATE_STOPPED = 0
    val STATE_STARTED = 1
    val STATE_PAUSED = 2
    val STATE_ROUND_OVER = 3
    val STATE_NEW_ROUND = 4

    private var state: Int = STATE_STOPPED


    fun bind(view: GameView) {
        this.view = view
        val gameId = GameFlow.currentGame!!.id
        val firebase = FirebaseFirestore.getInstance()
        firebaseCardsDataSource = FirebaseCardsDataSource(gameId, firebase)
        cardsRepository = CardsRepositoryImpl(firebaseCardsDataSource)

        firebasePlayersDataSource = FirebasePlayersDataSource(firebase)
        playersRepository = PlayersRepositoryImpl(firebasePlayersDataSource)

        firebaseGamesDataSource = FirebaseGamesDataSource(firebase)
        gamesRepository = GamesRepositoryImpl(firebaseGamesDataSource)

        playersObservable = ObservePlayers(playersRepository, schedulerProvider)
        cardsObservable = ObserveAllCards(cardsRepository, schedulerProvider)
        updateGame = AddGame(gamesRepository, schedulerProvider)
        observeGame = ObserveGame(gamesRepository, schedulerProvider)


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
                cardDeck = cards.toMutableList()
                view.updateCards(cards.filter { !it.used })
            }, {
                Timber.e(it, "error while observing cards")
            }).let {
                disposables.add(it)
            }

        observeGame(gameId)
            .distinctUntilChanged()
            .subscribe({newGame->
                val currentGame = GameFlow.currentGame!!
                val newPlayer = newGame.currentPlayer
                if (newPlayer?.id != currentGame.currentPlayer?.id) {
                    if (GameFlow.me == newPlayer) {
                        Timber.w("new player is me! newPlayer: ${newPlayer?.name}")
                        onPickNextCard()
                        setState(STATE_STARTED)
                    } else {
                        newPlayer?.let {
                            view.setCurrentOtherPlayer(newPlayer)
                        } ?: view.setNoCurrentPlayer()
                    }
                }
                view.setRound(newGame.currentRound.toString())
                if (currentGame.currentRound != newGame.currentRound) {
                    loadNewRound()
                }

                if (newGame.state is GameState.Finished) {
                    view.showGameOver()
                }
                GameFlow.updateGame(newGame)
            }, {
                Timber.e(it, "error while observing game")
            }).let {
                disposables.add(it)
            }
    }

    fun onNewRoundClick() {
        when {
            lastRound() -> {
                view.showLastRoundToast()
            }
            state == STATE_ROUND_OVER -> {
                setNextRound()
            }
            else -> {
                view.showNewRoundAlert { approved ->
                    if (approved) {
                        setNextRound()
                    }
                }
            }
        }
    }

    private fun setNextRound() {
        var gameRound = GameFlow.currentGame!!.state.gameInfo.round
        gameRound++
        setNewRound(gameRound)
            .subscribe({
                Timber.d("set new round success")
            }, {
                Timber.e(it, "error setNewRound")
            }).let {
                disposables.add(it)
            }
    }

    private fun onPlayerStarted() {
        setMeAsCurrentPlayer()
            .subscribe(
                { Timber.d("set me as current player success") },
                { Timber.e(it, "error while setting current player") }
            ).let { disposables.add(it) }
    }

    fun onPickNextCard() {
        val notUsedCards = unUsedCards()
        val card = if (notUsedCards.isNotEmpty()) notUsedCards.random().copy(used = true) else null
        if (card != null) {
            cardsRepository.updateCard(card)
                .subscribe({
                    lastCard = card
                    view.updateCard(card)
                }, {
                    Timber.e(it, "error while update card")
                }).let {
                    disposables.add(it)
                }
        } else {
            Timber.w("no un used cards left!")
            if (lastRound()) {
                setNewGameState(GameState.Finished(GameFlow.currentGame!!.state.gameInfo))
                    .subscribe(
                        { Timber.d("setNewGameState Finished success") },
                        { Timber.e(it, "error setNewGameState Finished") }
                    ).let { disposables.add(it) }
            } else {
                setState(STATE_ROUND_OVER)
            }
        }
    }

    private fun unUsedCards() = cardDeck.filter { !it.used }

    fun onPlayerResumedNewRound() {
        onPickNextCard()
        setState(STATE_STARTED)
    }

    fun onTurnEnded() {
        maybeFlipLastCard()
            .andThen(endMyTurn())
            .subscribe({
                setState(STATE_STOPPED)
            }, {
                Timber.e(it, "error onTurnEnded")
            }).let {
                disposables.add(it)
            }
    }

    fun onPlayerPaused() {
        setState(STATE_PAUSED)
    }

    fun onPlayerResumed() {
        setState(STATE_STARTED)
    }

    fun unBind() {
        disposables.clear()
    }

    private fun setState(state: Int) {
        this.state = state
        when (state) {
            STATE_STARTED -> view.setStartedState()
            STATE_STOPPED -> view.setStoppedState()
            STATE_PAUSED -> view.setPausedState()
            STATE_ROUND_OVER -> view.setRoundEndState()
            STATE_NEW_ROUND -> view.setPausedState()
        }
    }

    private fun loadNewRound() {
        setAllCardsToUnused()
        cardsRepository.updateCards(cardDeck)
            .subscribe({
                setState(STATE_NEW_ROUND)
                Timber.d("update cards success")
            }, {
                Timber.e(it, "error while update card")
            }).let {
                disposables.add(it)
            }
    }

    private fun lastRound(): Boolean  =
        GameFlow.currentGame?.state?.gameInfo?.round == 3

    private fun setMeAsCurrentPlayer(): Completable {
        val updatedGame =
            GameFlow.currentGame!!.copy(state = GameState.Started(GameInfo(currentPlayer = GameFlow.me!!)))
        return updateGame(updatedGame)
    }

    private fun endMyTurn(): Completable {
        val updatedGame =
            GameFlow.currentGame!!.copy(state = GameState.Started(GameInfo(currentPlayer = null)))
        return updateGame(updatedGame)
    }

    private fun setNewRound(round: Int): Completable {
        val game = GameFlow.currentGame!!
        return if (game.state is GameState.Started) {
            val updatedGame =
                game.copy(state = game.state.copy(gameInfo = game.state.gameInfo.copy(round = round)))
            updateGame(updatedGame)
        } else {
            Completable.complete()
        }
    }

    private fun setNewGameState(state: GameState): Completable =
        updateGame(GameFlow.currentGame!!.copy(state = state))

    private fun setAllCardsToUnused() {
        cardDeck.forEachIndexed { index, item ->
            cardDeck[index] = cardDeck[index].copy(used = false)
        }
    }

    private fun maybeFlipLastCard(): Completable =
        lastCard?.let {
            Timber.d("flipping last card: ${it.name}")
            cardsRepository.updateCard(it.copy(used = false))
        } ?: Completable.complete()

    fun onStartButtonClick() {
        when (state) {
            STATE_STOPPED -> onPlayerStarted()
            STATE_PAUSED -> onPlayerResumed()
            STATE_STARTED -> onPlayerPaused()
            STATE_ROUND_OVER -> {/* disabled */}
            STATE_NEW_ROUND -> onPlayerResumedNewRound()
        }
    }

    interface GameView{
        fun updateCards(cards: List<Card>)
        fun updateTeams(list: List<Player>)
        fun updateCard(card: Card)
        fun showGameOver()
        fun setCurrentOtherPlayer(player: Player)
        fun setPausedState()
        fun setStartedState()
        fun setStoppedState()
        fun setRoundEndState()
        fun setNoCurrentPlayer()
        fun setRound(toString: String)
        fun showNewRoundAlert(onClick: (Boolean) -> Unit)
        fun showLastRoundToast()
    }
}