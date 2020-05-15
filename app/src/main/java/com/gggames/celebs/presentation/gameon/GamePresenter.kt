package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.*
import com.gggames.celebs.model.RoundState.Ready
import com.gggames.celebs.model.TurnState.Running
import com.gggames.celebs.model.TurnState.Stopped
import com.gggames.celebs.utils.media.AudioPlayer
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamePresenter @Inject constructor(
    private val playersObservable: ObservePlayers,
    private val cardsObservable: ObserveAllCards,
    private val updateGame: SetGame,
    private val observeGame: ObserveGame,
    private val gameFlow: GameFlow,
    private val cardsRepository: CardsRepository,
    private val gamesRepository: GamesRepository,
    private val audioPlayer: AudioPlayer
) {
    private var cardDeck = mutableListOf<Card>()

    private var lastCard: Card? = null

    private val disposables = CompositeDisposable()
    private lateinit var view: GameView

    private val game: Game
        get() = gamesRepository.currentGame!!

    private var lastGame: Game? = null
    private val roundState : RoundState
    get() = game.gameInfo.round.state

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
        Timber.w("observeGame onNext. newP: ${newPlayer?.name}, curP: ${lastGame?.currentPlayer?.name}")
        if (newGame.round != lastGame?.round) {
            onRoundUpdate(newGame.gameInfo.round)
        }
        view.setTeamNames(newGame.teams)
        view.setScore(newGame.gameInfo.score)

        if (newGame.state == GameState.Finished) {
            view.showGameOver()
        }
        Timber.v("observeGame onNext: game: $newGame}")
        lastGame = newGame
//        gameFlow.updateGame(newGame)
    }

    private fun onRoundUpdate(newRound: Round) {
        Timber.v("UPDATE::ROUND:: newRound: $newRound}")
        val meActive = gameFlow.isMeActivePlayer(game)
        if (newRound != lastGame?.round) {
            view.setRound(newRound.roundNumber.toString())
            if (lastGame?.currentRound != newRound.roundNumber) {
                if (gameFlow.isMeActivePlayer(game)) {
                    loadNewRound()
                }
            }
            when (newRound.state) {
                Ready -> {
                }
                RoundState.Ended -> {
                    view.setRoundEndState(meActive)
                }
                RoundState.New -> {
                    view.setPausedState(meActive)
                }
            }
            if (newRound.turn != lastGame?.turn) {
                onTurnUpdate(newRound.turn)
            }
        }
    }

    private fun onTurnUpdate(turn: Turn) {
        Timber.v("UPDATE::TURN:: onTurnUpdate turn: $turn}")
        val meActive = gameFlow.isMeActivePlayer(game)
        if (gameFlow.isMeActivePlayer(game)) {
            when (turn.state) {
                Stopped -> {
                    view.setStoppedState()
                }
                Running -> {
                    view.setStartedState(meActive)
                }
                TurnState.Paused -> {
                    view.setPausedState(meActive)
                }
            }
        } else {
            when (turn.state) {
                Stopped -> {
                    view.setStoppedState()
                }
                Running -> {
                    turn.player?.let {
                        view.setStartedState(meActive)
                        view.setCurrentOtherPlayer(it)
                    } ?: view.setNoCurrentPlayer()
                }
                TurnState.Paused -> {
                    view.setPausedState(meActive)
                }
            }
        }
    }

    fun onNewRoundClick() {
        when {
            lastRound() -> {
                view.showLastRoundToast()
            }

            roundState == RoundState.Ended -> {
//            state == STATE_ROUND_OVER -> {
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
        setGameStateStartedAndMeActive()
            .andThen(pickNextCard())
            .andThen(setTurnState(Running))
            .subscribe(
                { Timber.d("set me as current player success") },
                { Timber.e(it, "error while setting current player") }
            ).let { disposables.add(it) }
    }

    private fun setGameStateStartedAndMeActive(): Completable =
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


    private fun gameInfoWith(turn: Turn): GameInfo =
        game.gameInfo.copy(
            round = game.gameInfo.round.copy(
                turn = turn
            )
        )

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
                .andThen(pickNextCard())
                .subscribe({
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

    private fun pickNextCard(): Completable {
        val notUsedCards = unUsedCards()
        val card = if (notUsedCards.isNotEmpty()) notUsedCards.random().copy(used = true) else null
        Timber.w("pickNextCard, card: $card")
        return if (card != null) {
            cardsRepository.updateCard(card)
                .andThen(
                    Completable.fromCallable {
                    lastCard = card
                    view.updateCard(card)
                    }
                )
        } else {
            Timber.w("no un used cards left!")
            if (lastRound()) {
                setNewGameState(GameState.Finished)
            } else {
//                setState(STATE_ROUND_OVER)
                setTurnState(Stopped)
                    .andThen(setRoundState(RoundState.Ended))
            }
        }
    }

    private fun setRoundState(state: RoundState): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(state = state)))
        return updateGame(newGame)
    }

    private fun setTurnState(state: TurnState): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(turn = game.gameInfo.round.turn.copy(state = state))))
        return updateGame(newGame)
    }


    private fun setNewGameState(state: GameState): Completable =
        updateGame(game.copy(state = state))

    private fun setNewGameInfo(gameInfo: GameInfo): Completable =
        updateGame(game.copy(gameInfo = gameInfo))

    private fun unUsedCards() = cardDeck.filter { !it.used }


    private fun onPlayerResumedNewRound() {
//        pickNextCard()
        setRoundState(Ready)
            .andThen(pickNextCard())
            .andThen(setTurnState(Running))
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
//        setState(STATE_STARTED)
    }

    fun onTurnEnded() {
        setTurnState(Stopped)
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
        if (gameFlow.isMeActivePlayer(game)) {
            maybeFlipLastCard()
                .andThen(endMyTurn())
                .subscribe({
                    view.setStoppedState()
                }, {
                    Timber.e(it, "error onTurnEnded")
                }).let {
                    disposables.add(it)
                }
        } else {
            view.setStoppedState()
            Timber.d("onTurnEnded, I'm not the active player. setting state to STOPPED")
        }
    }

    fun onPlayerPaused() {
        setTurnState(TurnState.Paused)
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
//        setState(STATE_PAUSED)
    }

    fun onPlayerResumed() {
        setTurnState(Running)
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
//        setState(STATE_STARTED)
    }

    fun unBind() {
        audioPlayer.release()
        disposables.clear()
    }

//    private fun setState(state: Int) {
//        this.state = state
//        when (state) {
//            STATE_STARTED -> view.setStartedState()
//            STATE_STOPPED -> view.setStoppedState()
//            STATE_PAUSED -> view.setPausedState()
//            STATE_ROUND_OVER -> view.setRoundEndState()
//            STATE_NEW_ROUND -> view.setPausedState()
//        }
//    }

    /*
    Load new round - only for active player
     */
    private fun loadNewRound() {
        setAllCardsToUnused()
        cardsRepository.updateCards(cardDeck)
            .subscribe({
//                setState(STATE_NEW_ROUND)
                setRoundState(RoundState.New).subscribe(
                    { Timber.d("setRoundState Ready success") },
                    { Timber.e(it, "error setRoundState Ready") }
                ).let { disposables.add(it) }
                Timber.d("update cards success")
            }, {
                Timber.e(it, "error while update card")
            }).let {
                disposables.add(it)
            }
    }

    private fun lastRound(): Boolean  =
        game.gameInfo.round.roundNumber == 3

    private fun endMyTurn(): Completable {
        val game = gameInfoWith(game.gameInfo.round.turn.copy(player = null, state = Stopped))
        Timber.v("endMyTurn, game: $game")
        return setNewGameInfo(game)
    }

    private fun setNewRound(round: Int): Completable =
        setNewGameInfo(game.gameInfo.copy(round = game.gameInfo.round.copy(roundNumber = round)))


    private fun setNewGameStateAndGameInfo(state: GameState, gameInfo: GameInfo): Completable {
        val newGame = (game.copy(state = state, gameInfo = gameInfo))
        return updateGame(newGame)
    }

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

    fun onStartButtonClick(buttonState: ButtonState) {
        Timber.d("---- startButton click, state: $buttonState, roundState: $roundState ----")
        when (buttonState) {
            ButtonState.Stopped -> onPlayerStarted()
            ButtonState.Running -> onPlayerPaused()
            ButtonState.Paused ->
            {
                if (roundState == RoundState.New) {
                    onPlayerResumedNewRound()
                } else {
                    onPlayerResumed()
                }

            }
        }


//        when (state) {
//            STATE_STOPPED -> onPlayerStarted()
//            STATE_PAUSED -> onPlayerResumed()
//            STATE_STARTED -> onPlayerPaused()
//            STATE_ROUND_OVER -> {/* disabled */}
//            STATE_NEW_ROUND -> onPlayerResumedNewRound() // only for active player
//        }
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
        fun setPausedState(meActive: Boolean)
        fun setStartedState(meActive: Boolean)
        fun setStoppedState()
        fun setRoundEndState(meActive: Boolean)
        fun setNoCurrentPlayer()
        fun setRound(toString: String)
        fun showNewRoundAlert(onClick: (Boolean) -> Unit)
        fun showLastRoundToast()
        fun setScore(score: Map<String, Int>)
        fun setTeamNames(teams: List<Team>)
    }
}
