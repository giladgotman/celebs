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
import com.gggames.celebs.model.TurnState.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.*
import com.gggames.celebs.utils.media.AudioPlayer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


const val TURN_TIME_MILLIS = 60000L
//const val TURN_TIME_MILLIS = 10000L


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

    fun bind(view: GameView, events: Observable<GameScreenContract.UiEvent>) {
        this.view = view
        val gameId = game.id

        events.subscribe(::handleUiEvent).let { disposables.add(it) }

        playersObservable(gameId)
            .distinctUntilChanged()
            .subscribe(::onPlayersChange).let { disposables.add(it) }

        cardsObservable()
            .distinctUntilChanged()
            .subscribe(::onCardsChange).let { disposables.add(it) }

        observeGame(gameId)
            .distinctUntilChanged()
            .subscribe(::onGameChange).let { disposables.add(it) }
    }

    private fun handleUiEvent(event: GameScreenContract.UiEvent) {
        when (event) {
            is RoundClick-> onNewRoundClick()
            is StartStopClick-> onStartButtonClick(event.buttonState, event.time)
            is CorrectClick-> onCorrectClick(event.time)
            is EndTurnClick -> onTimerEnd()
            is CardsAmountClick -> onCardsAmountClick()
            is TimerEnd -> onTimerEnd()
        }
    }

    private fun onCardsChange(cards: List<Card>) {
        cardDeck = cards.toMutableList()
        view.updateCards(cards.filter { !it.used })
    }

    private fun onPlayersChange(players: List<Player>) {
        val updatedTeams = game.teams.map { team ->
            team.copy(players = players.filter { it.team == team.name })
        }
        view.updateTeams(updatedTeams)
    }

    private fun onGameChange(newGame: Game) {
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
                Idle -> {
                    view.setStoppedState()
                }
                Stopped -> {
                    view.setStoppedState()
                }
                Running -> {
                    view.setStartedState(meActive)
                }
                Paused -> {
                    view.setPausedState(meActive)
                }
            }
        } else {
            when (turn.state) {
                Idle -> {
                    view.setStoppedState()
                }
                Stopped -> {
                    view.setStoppedState()
                    view.showTimesUp()
                }
                Running -> {
                    turn.player?.let {
                        view.setStartedState(meActive, turn.time)
                        view.setCurrentOtherPlayer(it)
                    } ?: view.setNoCurrentPlayer()
                }
                Paused -> {
                    view.setPausedState(meActive, turn.time)
                }
            }
        }
    }

    private fun onNewRoundClick() {
        when {
            lastRound() -> {
                view.showLastRoundToast()
            }
            roundState == RoundState.Ended -> {
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
            .andThen(handleNextCard(pickNextCard()))
            .andThen(setTurnState(Running))
            .subscribe(
                { Timber.d("set me as current player success") },
                { Timber.e(it, "error while setting current player") }
            ).let { disposables.add(it) }
    }

    private fun handleNextCard(card: Card?, time: Long? = null): Completable {
        return if (card != null) {
            cardsRepository.updateCard(card)
                .andThen(
                    Completable.fromCallable {
                        lastCard = card
                        view.updateCard(card)
                        view.setCorrectEnabled(true)
                    }
                )
        } else {
            Timber.w("no un used cards left!")
            if (lastRound()) {
                setNewGameState(GameState.Finished)
            } else {
                val turn = time?.let { game.gameInfo.round.turn.copy(state = Paused, time = it) }
                    ?: game.gameInfo.round.turn.copy(state = Paused)
                setNewGameInfo(gameInfoWith(turn))
                    .andThen(setRoundState(RoundState.Ended))
            }
        }
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

    private fun onCorrectClick(time: Long) {
        view.setCorrectEnabled(false)
        gameFlow.me?.team?.let {
            increaseScore(it)
                .andThen(handleNextCard(pickNextCard(), time))
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

    private fun pickNextCard(): Card? {
        val notUsedCards = unUsedCards()
        val card = if (notUsedCards.isNotEmpty()) notUsedCards.random().copy(used = true) else null
        Timber.w("pickNextCard, card: $card")
        return card
    }

    private fun setRoundState(state: RoundState): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(state = state)))
        return updateGame(newGame)
    }

    private fun setTurnState(state: TurnState): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(turn = game.gameInfo.round.turn.copy(state = state))))
        return updateGame(newGame)
    }
    private fun setTurnStoppedState(): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(turn = game.gameInfo.round.turn.copy(state = Stopped, time = TURN_TIME_MILLIS))))
        return updateGame(newGame)
    }



    private fun setNewGameState(state: GameState): Completable =
        updateGame(game.copy(state = state))

    private fun setNewGameInfo(gameInfo: GameInfo): Completable =
        updateGame(game.copy(gameInfo = gameInfo))

    private fun unUsedCards() = cardDeck.filter { !it.used }


    private fun onPlayerResumedNewRound() {
        setRoundState(Ready)
            .andThen(handleNextCard(pickNextCard()))
            .andThen(setTurnState(Running))
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
    }

    private fun onTurnEnded() {
        if (gameFlow.isMeActivePlayer(game)) {
            view.setStoppedState()
            setTurnStoppedState()
                .andThen(maybeFlipLastCard())
                .andThen(endMyTurn())
                .subscribe({}, { Timber.e(it, "error onTurnEnded") }).let {
                    disposables.add(it)
                }
        }
    }

    private fun onPlayerPaused(time: Long?) {
        val newTurn = time?.let { game.turn.copy(state = Paused, time = it) }
            ?: game.turn.copy(state = Paused)
        setNewGameInfo(gameInfoWith(newTurn))
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
    }

    private fun onPlayerResumed(time: Long?) {
        val newTurn = time?.let { game.turn.copy(state = Running, time = it) }
            ?: game.turn.copy(state = Running)
        setNewGameInfo(gameInfoWith(newTurn))
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
    }

    fun unBind() {
        audioPlayer.release()
        disposables.clear()
    }
    /*
    Load new round - only for active player
     */
    private fun loadNewRound() {
        setAllCardsToUnused()
        cardsRepository.updateCards(cardDeck)
            .subscribe({
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

    private fun onStartButtonClick(buttonState: ButtonState, time: Long?) {
        Timber.d("---- startButton click, state: $buttonState, roundState: $roundState ----")
        when (buttonState) {
            ButtonState.Stopped -> onPlayerStarted()
            ButtonState.Running -> onPlayerPaused(time)
            ButtonState.Paused ->
            {
                if (roundState == RoundState.New) {
                    onPlayerResumedNewRound()
                } else {
                    onPlayerResumed(time)
                }

            }
        }
    }

   private fun onTimerEnd() {
        if (gameFlow.isMeActivePlayer(game)) {
            audioPlayer.play("timesupyalabye")
            view.showTimesUp()
        }
        onTurnEnded()
    }

    private fun onCardsAmountClick() {
        if (game.round.state == RoundState.Ended) {
            view.showAllCards(cardDeck)
        }
    }

    interface GameView{
        fun updateCards(cards: List<Card>)
        fun updateTeams(teams: List<Team>)
        fun updateCard(card: Card)
        fun showGameOver()
        fun setCurrentOtherPlayer(player: Player)
        fun setPausedState(meActive: Boolean, time: Long? = null)
        fun setStartedState(meActive: Boolean, time: Long? = null)
        fun setStoppedState()
        fun setRoundEndState(meActive: Boolean)
        fun setNoCurrentPlayer()
        fun setRound(toString: String)
        fun showNewRoundAlert(onClick: (Boolean) -> Unit)
        fun showLastRoundToast()
        fun setScore(score: Map<String, Int>)
        fun setTeamNames(teams: List<Team>)
        fun showTimesUp()
        fun setCorrectEnabled(enabled: Boolean)
        fun showAllCards(cardDeck: List<Card>)
    }
}
