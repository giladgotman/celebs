package com.gggames.celebs.presentation

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.cards.CardsRepositoryImpl
import com.gggames.celebs.data.games.GamesRepository
import com.gggames.celebs.data.games.GamesRepositoryImpl
import com.gggames.celebs.data.model.*
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

    private val game: Game
        get() = GameFlow.currentGame!!

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
                val newPlayer = newGame.currentPlayer
                Timber.w("observeGame onNext. newP: ${newPlayer?.name}, curP: ${game.currentPlayer?.name}")
                if (newPlayer?.id != game.currentPlayer?.id) {
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
                if (game.currentRound != newGame.currentRound) {
                    if (GameFlow.me == newPlayer) {
                        loadNewRound()
                    }
                }

                if (newGame.state == GameStateE.Finished) {
                    view.showGameOver()
                }
                Timber.v("observeGame onNext: game: $newGame}")
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
        var gameRound = game.gameInfo.round
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
        setStartedAndMeActive()
            .subscribe(
                { Timber.d("set me as current player success") },
                { Timber.e(it, "error while setting current player") }
            ).let { disposables.add(it) }
    }

    private fun setStartedAndMeActive(): Completable =
        when (game.state) {
            GameStateE.Created -> {
                setNewGameStateAndGameInfo(
                    GameStateE.Started,
                    game.gameInfo.copy(currentPlayer = GameFlow.me!!)
                )
            }
            GameStateE.Started -> {
                setNewGameInfo(game.gameInfo.copy(currentPlayer = GameFlow.me!!))
            }
            else -> {
                Completable.complete()
            }
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
                setNewGameState(GameStateE.Finished)
                    .subscribe(
                        { Timber.d("setNewGameState Finished success") },
                        { Timber.e(it, "error setNewGameState Finished") }
                    ).let { disposables.add(it) }
            } else {
                setState(STATE_ROUND_OVER)
            }
        }
    }

    private fun setNewGameState(state: GameStateE): Completable =
        updateGame(GameFlow.currentGame!!.copy(state = state))

    private fun setNewGameInfo(gameInfo: GameInfo): Completable =
        updateGame(GameFlow.currentGame!!.copy(gameInfo = gameInfo))

    private fun unUsedCards() = cardDeck.filter { !it.used }


    private fun onPlayerResumedNewRound() {
        onPickNextCard()
        setState(STATE_STARTED)
    }

    fun onTurnEnded() {
        if (GameFlow.isActivePlayer()) {
            maybeFlipLastCard()
                .andThen(endMyTurn())
                .subscribe({
                    setState(STATE_STOPPED)
                }, {
                    Timber.e(it, "error onTurnEnded")
                }).let {
                    disposables.add(it)
                }
        } else {
            setState(STATE_STOPPED)
            Timber.d("onTurnEnded, I'm not the active player. setting state to STOPPED")
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

    /*
    Load new round - only for active player
     */
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
        game.gameInfo.round == 3

    private fun endMyTurn(): Completable =
        setNewGameInfo(game.gameInfo.copy(currentPlayer = null))

    private fun setNewRound(round: Int): Completable =
        setNewGameInfo(game.gameInfo.copy(round = round))


    private fun setNewGameStateAndGameInfo(state: GameStateE, gameInfo: GameInfo): Completable =
        updateGame(game.copy(state = state, gameInfo = gameInfo))

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
        Timber.d("---- startButton click, state: $state ----")
        when (state) {
            STATE_STOPPED -> onPlayerStarted()
            STATE_PAUSED -> onPlayerResumed()
            STATE_STARTED -> onPlayerPaused()
            STATE_ROUND_OVER -> {/* disabled */}
            STATE_NEW_ROUND -> onPlayerResumedNewRound() // only for active player
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