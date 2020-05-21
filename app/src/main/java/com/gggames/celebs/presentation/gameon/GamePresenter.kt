package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.games.domain.AddGame
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.*
import com.gggames.celebs.utils.media.AudioPlayer
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamePresenter @Inject constructor(
    private val playersObservable: ObservePlayers,
    private val cardsObservable: ObserveAllCards,
    private val updateGame: AddGame,
    private val observeGame: ObserveGame,
    private val gameFlow: GameFlow,
    private val cardsRepository: CardsRepository,
    private val audioPlayer: AudioPlayer
) {
    private var cardDeck = mutableListOf<Card>()

    private var lastCard: Card? = null

    private val disposables = CompositeDisposable()
    private lateinit var view: GameView

    private val game: Game
        get() = gameFlow.currentGame!!

    val STATE_STOPPED = 0
    val STATE_STARTED = 1
    val STATE_PAUSED = 2
    val STATE_ROUND_OVER = 3
    val STATE_NEW_ROUND = 4

    private var state: Int = STATE_STOPPED

    fun bind(view: GameView) {
        this.view = view
        val gameId = game.id

        playersObservable(gameId)
            .distinctUntilChanged()
            .subscribe({ players ->
                onUpdatePlayers(players)
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
                onGameUpdate(newGame)
            }, {
                Timber.e(it, "error while observing game")
            }).let {
                disposables.add(it)
            }
    }

    private fun onUpdatePlayers(players: List<Player>) {
        val updatedTeams = game.teams.map { team ->
            team.copy(players = players.filter { it.team == team.name })
        }
        view.updateTeams(updatedTeams)
    }

    private fun onGameUpdate(newGame: Game) {
        val newPlayer = newGame.currentPlayer
        Timber.w("observeGame onNext. newP: ${newPlayer?.name}, curP: ${game.currentPlayer?.name}")
        if (newPlayer?.id != game.currentPlayer?.id) {
            if (gameFlow.me == newPlayer) {
                Timber.w("new player is me! newPlayer: ${newPlayer?.name}")
                pickNextCard()
                setState(STATE_STARTED)
            } else {
                newPlayer?.let {
                    view.setCurrentOtherPlayer(newPlayer)
                } ?: view.setNoCurrentPlayer()
            }
        }
        view.setRound(newGame.currentRound.toString())
        view.setTeamNames(newGame.teams)
        if (game.currentRound != newGame.currentRound) {
            if (gameFlow.me == newPlayer) {
                loadNewRound()
            }
        }

        view.setScore(newGame.gameInfo.score)

        if (newGame.state == GameState.Finished) {
            view.showGameOver()
        }
        Timber.v("observeGame onNext: game: $newGame}")
        gameFlow.updateGame(newGame)
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
        var gameRound = game.gameInfo.round.roundNumber
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
            GameState.Created -> {
                setNewGameStateAndGameInfo(
                    GameState.Started,
                    gameInfoWith(gameFlow.me!!)
                )
            }
            GameState.Started -> {
                setNewGameInfo(gameInfoWith(gameFlow.me!!))
            }
            else -> {
                Completable.complete()
            }
        }

    private fun gameInfoWith(player: Player?): GameInfo =
        game.gameInfo.copy(
            round = game.gameInfo.round.copy(
                turn = game.gameInfo.round.turn.copy(
                    player = player
                )
            )
        )

    fun onCorrectClick() {
        gameFlow.me?.team?.let {
            increaseScore(it)
                .subscribe({
                    pickNextCard()
                }, {
                    Timber.e(it, "error while increaseScore")
                }).let { disposables.add(it) }
        }

    }

    private fun increaseScore(teamName: String): Completable {
        val currScore = game.gameInfo.score[teamName]
        currScore?.let {
            val mutableMap = game.gameInfo.score.toMutableMap()
            mutableMap[teamName] = currScore + 1
            return setNewGameInfo(game.gameInfo.copy(score = mutableMap))
        }
        return Completable.complete()
    }

    private fun pickNextCard() {
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
                setNewGameState(GameState.Finished)
                    .subscribe(
                        { Timber.d("setNewGameState Finished success") },
                        { Timber.e(it, "error setNewGameState Finished") }
                    ).let { disposables.add(it) }
            } else {
                setState(STATE_ROUND_OVER)
            }
        }
    }

    private fun setNewGameState(state: GameState): Completable =
        updateGame(game.copy(state = state))

    private fun setNewGameInfo(gameInfo: GameInfo): Completable =
        updateGame(game.copy(gameInfo = gameInfo))

    private fun unUsedCards() = cardDeck.filter { !it.used }


    private fun onPlayerResumedNewRound() {
        pickNextCard()
        setState(STATE_STARTED)
    }

    fun onTurnEnded() {
        if (gameFlow.isActivePlayer()) {
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
        audioPlayer.release()
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
        game.gameInfo.round.roundNumber == 3

    private fun endMyTurn(): Completable =
        setNewGameInfo(gameInfoWith(null))

    private fun setNewRound(round: Int): Completable =
        setNewGameInfo(game.gameInfo.copy(round = game.gameInfo.round.copy(roundNumber = round)))


    private fun setNewGameStateAndGameInfo(state: GameState, gameInfo: GameInfo): Completable =
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

    fun onTimesUp() {
        audioPlayer.play("timesupyalabye")
        onTurnEnded()
    }

    interface GameView{
        fun updateCards(cards: List<Card>)
        fun updateTeams(teams: List<Team>)
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
        fun setScore(score: Map<String, Int>)
        fun setTeamNames(teams: List<Team>)
    }
}