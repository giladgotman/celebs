package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.LeaveGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.*
import com.gggames.celebs.model.RoundState.Ended
import com.gggames.celebs.model.RoundState.Ready
import com.gggames.celebs.model.TurnState.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.*
import com.gggames.celebs.utils.media.AudioPlayer
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

//const val TURN_TIME_MILLIS = 60000L
const val TURN_TIME_MILLIS = 10000L

class GamePresenter @Inject constructor(
    private val playersObservable: ObservePlayers,
    private val cardsObservable: ObserveAllCards,
    private val updateGame: SetGame,
    private val observeGame: ObserveGame,
    private val authenticator: Authenticator,
    private val cardsRepository: CardsRepository,
    private val gamesRepository: GamesRepository,
    private val leaveGame: LeaveGame,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
    private var cardDeck = mutableListOf<Card>()

    private var lastCard: Card? = null

    private val disposables = CompositeDisposable()
    private lateinit var view: GameView

    private val game: Game
        get() = gamesRepository.currentGame!!

    private var lastGame: Game? = null
    private val roundState: RoundState
    get() = game.gameInfo.round.state

    private var cardsFoundInTurn = mutableListOf<Card>()

    fun bind(view: GameView, events: Observable<GameScreenContract.UiEvent>) {
        this.view = view
        val gameId = game.id

        events.subscribe(::handleUiEvent).let { disposables.add(it) }

        playersObservable(gameId)
            .distinctUntilChanged()
            .compose(schedulerProvider.applyDefault())
            .subscribe(::onPlayersChange).let { disposables.add(it) }

        cardsObservable()
            .compose(schedulerProvider.applyDefault())
            .subscribe(::onCardsChange).let { disposables.add(it) }

        observeGame(gameId)
            .distinctUntilChanged()
            .compose(schedulerProvider.applyDefault())
            .subscribe(::onGameChange).let { disposables.add(it) }
    }

    private fun handleUiEvent(event: GameScreenContract.UiEvent) {
        when (event) {
            is RoundClick -> onNewRoundClick(event.time)
            is StartStopClick -> onStartButtonClick(event.buttonState, event.time)
            is CorrectClick -> onCorrectClick(event.time)
            is EndTurnClick -> onEndTurnClick()
            is CardsAmountClick -> onCardsAmountClick()
            is TimerEnd -> onTimerEnd()
            is OnBackPressed -> onBackPressed()
            is UserApprovedQuitGame -> onUserApprovedQuitGame()
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
        view.setTeams(newGame.teams)

        if (newGame.state == GameState.Finished) {
            releaseAll()
            view.navigateToEndGame()
        }
        Timber.v("observeGame onNext: game: $newGame}")
        lastGame = newGame
    }

    private fun onRoundUpdate(newRound: Round) {
        Timber.v("UPDATE::ROUND:: newRound: $newRound}")
        val meActive = authenticator.isMyselfActivePlayerBlocking(game)
        if (newRound != lastGame?.round) {
            view.setRound(newRound.roundNumber.toString())
            when (newRound.state) {
                Ready -> {
                }
                Ended -> {
                    view.setRoundEndState(meActive, newRound.roundNumber)
                    view.showRoundEnded(newRound, game.teams)
                }
                RoundState.New -> {
                    val startButtonEnabled = meActive || game.currentPlayer == null
                    view.setNewRound(startButtonEnabled, newRound.roundNumber)
                }
            }
            if (newRound.turn != lastGame?.turn) {
                onTurnUpdate(newRound.turn)
            }
        }
    }

    private fun onTurnUpdate(newTurn: Turn) {
        Timber.v("UPDATE::TURN:: onTurnUpdate turn: $newTurn}")
        val meActive = authenticator.isMyselfActivePlayerBlocking(game)
        val playButtonEnabled = meActive || game.currentPlayer == null
        if (authenticator.isMyselfActivePlayerBlocking(game)) {
            when (newTurn.state) {
                Idle -> {
                    view.setTurnStoppedState()
                }
                Stopped -> {
                    if (lastGame?.turn?.state != newTurn.state) {
                        view.setTurnStoppedState()
                        if (!quitingGame) {
                            showEndOfTurn()
                        }
                    }
                }
                Running -> {
                    view.setStartedState(meActive)
                }
                Paused -> {
                    view.setPausedState(playButtonEnabled)
                }
            }
        } else {
            when (newTurn.state) {
                Idle -> {
                    view.setTurnStoppedState()
                }
                Stopped -> {
                    if (lastGame?.turn?.state != newTurn.state) {
                        view.setTurnStoppedState()
                        showEndOfTurn()
                    }
                }
                Running -> {
                    newTurn.player?.let {
                        view.setStartedState(meActive, newTurn.time)
                        view.setCurrentOtherPlayer(it)
                    } ?: view.setNoCurrentPlayer()
                }
                Paused -> {
                    view.setPausedState(playButtonEnabled, newTurn.time)
                }
            }
        }
    }

    private fun showEndOfTurn() {
        val cards = cardDeck.filter { it.id in lastGame?.round?.turn?.cardsFound ?: emptyList() }

        Timber.d("showEndOfTurn, lastGame.player: ${lastGame?.round?.turn?.player}, game.player: ${game.round.turn.player}")
        view.showTurnEnded(lastGame?.round?.turn?.player, cards, lastGame?.round?.roundNumber ?: 1)
    }
    private fun onNewRoundClick(time: Long) {
        when {
            lastRound() -> {
                view.showLastRoundToast()
            }
            roundState == Ended -> {
                setNextRound()
            }
            else -> {
                view.showNewRoundAlert { approved ->
                    if (approved) {
                        endCurrentRound(time)
                            .subscribe {
                                setNextRound()
                            }
                    }
                }
            }
        }
    }

    private fun setNextRound() {
        resetDeck()
            .andThen(setNewRound(game.gameInfo.round.roundNumber + 1))
            .subscribe({
                Timber.d("set new round success")
            }, {
                Timber.e(it, "error setNewRound")
            }).let {
                disposables.add(it)
            }
    }

    private fun onPlayerStarted() {
        cardsFoundInTurn.clear()
        setGameStateStartedAndMeActive()
            .andThen(setRoundState(Ready))
            .andThen(handleNextCard(pickNextCard()))
            .andThen(setTurnStateAndLastCards(Running, cardsFoundInTurn.mapNotNull { it.id }))
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
                endCurrentRound(time)
            }
        }
    }

    private fun endCurrentRound(time: Long?): Completable {
        return if (game.round.state == Ended) {
            Completable.complete()
        } else {
            val turn = time?.let { game.gameInfo.round.turn.copy(state = Paused, time = it) }
                ?: game.gameInfo.round.turn.copy(state = Paused)
            return setNewGameInfo(gameInfoWith(turn))
                .andThen(setRoundState(RoundState.Ended))
        }
    }

    private fun setGameStateStartedAndMeActive(): Completable =
        when (game.state) {
            GameState.Created -> {
                setNewGameStateAndGameInfo(
                    GameState.Started,
                    gameInfoWith(authenticator.me!!)
                )
            }
            GameState.Started -> {
                setNewGameInfo(gameInfoWith(authenticator.me!!))
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
        lastCard?.let {
            cardsFoundInTurn.add(it)
        }
        authenticator.me?.team?.let {
            increaseScore(it)
                .andThen(setTurnTime(time))
                .andThen(setTurnLastCards(cardsFoundInTurn.mapNotNull { it.id }))
                .andThen(handleNextCard(pickNextCard(), time))
                .subscribe({
                }, {
                    Timber.e(it, "error while increaseScore")
                }).let { disposables.add(it) }
        }
    }

    private fun increaseScore(teamName: String): Completable {
        val teamIndex = game.teams.indexOfFirst { it.name == teamName }
        if (teamIndex != -1) {
            val currScore = game.teams[teamIndex].score
            val mutableTeams = game.teams.toMutableList()
            val updatedTeam = mutableTeams[teamIndex].copy(score = currScore + 1)
            mutableTeams[teamIndex] = updatedTeam
            return updateGame(game.copy(teams = mutableTeams))
        }
        return Completable.complete()
    }

    private fun pickNextCard(): Card? {
        val notUsedCards = unUsedCards()
        val card = if (game.type == GameType.Gift && game.round.roundNumber == 1) {
            if (notUsedCards.isNotEmpty()) notUsedCards.first().copy(used = true) else null
        } else {
            if (notUsedCards.isNotEmpty()) notUsedCards.random().copy(used = true) else null
        }
        Timber.w("pickNextCard, card: $card")
        return card
    }

    private fun setRoundState(state: RoundState): Completable {
        val newGame = game.copy(gameInfo = game.gameInfo.copy(round = game.gameInfo.round.copy(state = state)))
        return updateGame(newGame)
    }

    private fun setTurnState(state: TurnState): Completable {
        val newGame = game.copy(
            gameInfo = game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    turn = game.gameInfo.round.turn.copy(state = state)
                )
            )
        )
        return updateGame(newGame)
    }

    private fun setTurnTime(time: Long): Completable {
        val newGame = game.copy(
            gameInfo = game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    turn = game.gameInfo.round.turn.copy(time = time)
                )
            )
        )
        return updateGame(newGame)
    }

    private fun setTurnLastCards(cardsIds: List<String>): Completable {
        val newGame = game.copy(
            gameInfo = game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    turn = game.gameInfo.round.turn.copy(
                        cardsFound = cardsIds
                    )
                )
            )
        )
        return updateGame(newGame)
    }

    private fun setTurnStateAndLastCards(state: TurnState, cardsIds: List<String>): Completable {
        val newGame = game.copy(
            gameInfo = game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    turn = game.gameInfo.round.turn.copy(
                        state = state,
                        cardsFound = cardsIds
                    )
                )
            )
        )
        return updateGame(newGame)
    }

    private fun setTurnStoppedState(): Completable {
        val newGame = game.copy(
            gameInfo = game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    turn = game.gameInfo.round.turn.copy(
                        state = Stopped,
                        time = TURN_TIME_MILLIS
                    )
                )
            )
        )
        return updateGame(newGame)
    }

    private fun setNewGameState(state: GameState): Completable =
        updateGame(game.copy(state = state))

    private fun setNewGameInfo(gameInfo: GameInfo): Completable =
        updateGame(game.copy(gameInfo = gameInfo))

    private fun unUsedCards() = cardDeck.filter { !it.used }

    private fun onPlayerResumedNewRound() {
        setGameStateStartedAndMeActive()
            .andThen(setRoundState(Ready))
            .andThen(handleNextCard(pickNextCard()))
            .andThen(setTurnState(Running))
            .subscribe({}, { Timber.e(it) }).let { disposables.add(it) }
    }

    private fun onBackPressed() {
        if (authenticator.isMyselfActivePlayerBlocking(game)) {
            view.showLeaveGameDialog()
        } else {
            disposables.clear()
            view.navigateToGames()
        }
    }

    var quitingGame = false
    private fun onUserApprovedQuitGame() {
        quitingGame = true
        endMyTurn()
            .doAfterTerminate {
                releaseAll()
                view.navigateToGames()
            }
            .subscribe({}, {}
            ).let { disposables.add(it) }
    }

    fun onLogout(): Completable =
        endMyTurnIfImActiveBlocking()
            .andThen(leaveGame(game, authenticator.me!!))

    // TODO: 20.06.20 fix setGame side effects and make it non blocking
    private fun endMyTurnIfImActiveBlocking(): Completable {
        return if (authenticator.isMyselfActivePlayerBlocking(game)) {
            endMyTurn()
        } else {
            Completable.complete()
        }
    }

    private fun endMyTurn(): Completable =
        maybeFlipLastCard()
            .andThen(setTurnStoppedState())
            .andThen(setNullTurnPlayer())

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
        releaseAll()
    }

    private fun releaseAll() {
//        audioPlayer.release()
        disposables.clear()
    }

    /*
    Load new round - only for active player
     */
    private fun resetDeck(): Completable =
        setAllCardsToUnused()
            .andThen(cardsRepository.updateCards(cardDeck))

    private fun lastRound(): Boolean =
        game.gameInfo.round.roundNumber == 3

    private fun setNullTurnPlayer(): Completable {
        val game = gameInfoWith(game.gameInfo.round.turn.copy(player = null))
        return setNewGameInfo(game)
    }

    private fun setNewRound(round: Int): Completable =
        setNewGameInfo(
            game.gameInfo.copy(
                round = game.gameInfo.round.copy(
                    roundNumber = round,
                    state = RoundState.New
                )
            )
        )

    private fun setNewGameStateAndGameInfo(state: GameState, gameInfo: GameInfo): Completable {
        val newGame = (game.copy(state = state, gameInfo = gameInfo))
        return updateGame(newGame)
    }

    private fun setAllCardsToUnused() =
        Completable.fromCallable {
            cardDeck.forEachIndexed { index, item ->
                cardDeck[index] = cardDeck[index].copy(used = false)
            }
        }.doOnComplete { Timber.w("flipped cards. card: ${cardDeck[0]}") }

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
       if (authenticator.isMyselfActivePlayerBlocking(game)) {
           audioPlayer.play("timesupyalabye")
           endMyTurn().subscribe(
               {},
               { Timber.e(it, "error onTimerEnd") }
           ).let { disposables.add(it) }
       }

   }

    private fun onEndTurnClick() {
        // not used - removed button
    }

    private fun onCardsAmountClick() {
        if (game.round.state == RoundState.Ended) {
            view.showAllCards(cardDeck)
        }
    }

    interface GameView {
        fun updateCards(cards: List<Card>)
        fun updateTeams(teams: List<Team>)
        fun updateCard(card: Card)
        fun setCurrentOtherPlayer(player: Player)
        fun setPausedState(playButtonEnabled: Boolean, time: Long? = null)
        fun setStartedState(meActive: Boolean, time: Long? = null)
        fun setTurnStoppedState()
        fun setRoundEndState(meActive: Boolean, roundNumber: Int)
        fun setNoCurrentPlayer()
        fun setRound(toString: String)
        fun showNewRoundAlert(onClick: (Boolean) -> Unit)
        fun showLastRoundToast()
        fun setTeams(teams: List<Team>)
        fun showTurnEnded(player: Player?, cards: List<Card>, roundNumber: Int)
        fun showTurnEndedActivePlayer()
        fun setCorrectEnabled(enabled: Boolean)
        fun showAllCards(cardDeck: List<Card>)
        fun navigateToGames()
        fun navigateToEndGame()
        fun setNewRound(playButtonEnabled: Boolean, roundNumber: Int)
        fun showRoundEnded(
            round: Round,
            teams: List<Team>
        )
        fun showLeaveGameDialog()
        fun showCorrectCard(card: Card, videoUrl: String?)
    }
}
