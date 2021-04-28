package com.gggames.hourglass.presentation.gameon

import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.features.cards.domain.ObserveAllCards
import com.gggames.hourglass.features.gameon.*
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.ObserveGame
import com.gggames.hourglass.features.games.domain.ShowRoundInstructions
import com.gggames.hourglass.features.players.domain.ObservePlayers
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent.CorrectClick
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent.StartStopClick
import com.gggames.hourglass.utils.doInDebug
import com.gggames.hourglass.utils.media.AudioPlayer
import com.gggames.hourglass.utils.rx.ofType
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Observable.*
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject


const val TURN_TIME_MILLIS = 60000L

class GamePresenterMVI @Inject constructor(
    private val playersObservable: ObservePlayers,
    private val cardsObservable: ObserveAllCards,
    private val observeGame: ObserveGame,
    private val authenticator: Authenticator,
    private val gamesRepository: GamesRepository,
    private val handleNextCard: HandleNextCard,
    private val startGame: StartGame,
    private val pauseTurn: PauseTurn,
    private val resumeTurn: ResumeTurn,
    private val startRound: StartRound,
    private val handleCorrectCard: HandleCorrectCard,
    private val handleBackPressed: HandleBackPressed,
    private val handleEndTurnPressed: HandleEndTurnPressed,
    private val endTurn: EndTurn,
    private val flipLastCard: FlipLastCard,
    private val checkIfTurnStarted: CheckIfTurnStarted,
    private val showRoundInstructions: ShowRoundInstructions,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
    private var cardDeck = listOf<Card>()
    private var teamsWithPlayers = listOf<TeamWithPlayers>()

    private var lastCard: Card? = null
    private val disposables = CompositeDisposable()

    private var lastGame: Game? = null

    private val _states = PublishSubject.create<State>()
    private val _triggers = PublishSubject.create<Trigger>()
    val states: Observable<State> = _states
    val triggers: Observable<Trigger> = _triggers

    fun bind(events: Observable<UiEvent>) {
        val uiEvent = events
            .doOnNext { Timber.i("USER:: $it") }

        val dataInput =
            gamesRepository.getCurrentGame().toObservable().switchMap { game ->
                combineLatest(
                    observeGame(game.id).doOnNext { Timber.d("GameUpdate") },
                    playersObservable(game.id).doOnNext { Timber.d("PlayersUpdate") },
                    cardsObservable().doOnNext { Timber.d("CardsUpdate") },
                    Function3 { game: GameUpdate, players: PlayersUpdate, cards: CardsUpdate ->
                        CombinedGameUpdate(game.game, players.players, cards.cards)
                    }
                )
            }

        val allInput = merge(
            uiEvent.toResult(),
            dataInput,
            showRoundInstructions(),
            checkIfTurnStarted()
        )

        allInput
            .subscribeOn(schedulerProvider.io())
            .doOnNext {
                Timber.i("RESULT:: ${it.javaClass}")
                Timber.v("RESULT:: $it")
            }
            .share()
            .also { results ->
                results.scan(State.initialState, reduce())
                    .distinctUntilChanged()
                    .doOnNext { Timber.v("STATE:: \n$it") }
                    .observeOn(schedulerProvider.ui())
                    .subscribe({
                        _states.onNext(it)
                    }, { Timber.e(it, "Unhandled exception in the main stream") })
                    .let { disposables.add(it) }

                results
                    .observeOn(schedulerProvider.ui())
                    .compose { o ->
                        mergeArray(
                            o.ofType<ShowAllCardsResult>().flatMap<Trigger> { just(Trigger.ShowAllCards(it.cards)) },
                            o.ofType<StartedGameResult>().flatMap<Trigger> { just(Trigger.StartTimer) },
                            o.ofType<ShowCardInfoResult>().flatMap<Trigger> { just(Trigger.ShowCardInfo(it.cardName)) }
                        )
                    }
                    .doOnNext { Timber.i("TRIGGER:: $it") }
                    .subscribe({
                        _triggers.onNext(it)
                    }, { Timber.e(it, "Unhandled exception in the main triggers stream") })
                    .let { disposables.add(it) }
            }
    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is CombinedGameUpdate -> {
                val meActive = authenticator.isMyselfActivePlayerBlocking(result.game)
                val turnState = result.game.gameInfo.round.turn.state
                val turnOver = result.game.turn.state == TurnState.Over &&
                        result.game.round.state == RoundState.Started
                val roundOver = result.game.round.state == RoundState.Ended && previous.round.state != RoundState.Ended

                val teamPlayers =
                    result.game.teams.map { team -> team.toTeamWithPlayers(result.players.filter { it.team == team.name }) }

                val newState = previous.copy(
                    // Game
                    teamsWithScore = result.game.teams,
                    round = result.game.round,
                    isTimerRunning = turnState == TurnState.Running,
                    playButtonState = PlayButtonState(
                        isEnabled = meActive || result.game.currentPlayer == null,
                        state = turnState.toPlayButtonState()
                    ),
                    resetTime = !turnState.isTurnOn(),
                    showEndOfTurn = turnOver,
                    showEndOfRound = roundOver,
                    previousRoundName = lastGame?.round?.roundNumber?.toString() ?: previous.previousRoundName,
                    showGameOver = result.game.state == GameState.Finished,
                    currentCard = result.game.turn.currentCard,
                    currentPlayer = result.game.turn.player,
                    revealCurrentCard = meActive,
                    correctButtonEnabled = meActive && turnState == TurnState.Running,
                    helpButtonEnabled = meActive && turnState == TurnState.Running,
                    lastPlayer = result.game.currentPlayer ?: previous.lastPlayer,
                    cardsFoundInTurn = cardDeck.filter { it.id in result.game.turn.cardsFound },
                    time = result.game.turn.time,
                    // Players
                    teamsWithPlayers = teamPlayers,
                    nextPlayer = result.game.turn.nextPlayer ?: result.game.host,
                    // Cards
                    cardsInDeck = result.cards.filter { !it.used }.size,
                    totalCardsInGame = result.cards.size,
                    useLocalTimer = true,
                    screenTitle = result.game.name,
                    isEndTurnEnabled = meActive,
                    isCardsAmountEnabled = result.game.round.let { it.state == RoundState.New && it.roundNumber == 2 },
                    meActive = meActive
                )
                // TODO: 25.10.20 remove from here and make it a pure function
                lastGame = result.game
                lastCard = result.game.turn.currentCard
                cardDeck = result.cards
                teamsWithPlayers = teamPlayers

                doInDebug {
                    previous.printDiff(newState)
                }
                newState
            }
            is ShowRoundInstructionsResult -> previous.copy(showRoundInstructions = result.show)
            is GameUpdate -> {
                previous
            }
            is PlayersUpdate -> {
                previous
            }
            is CardsUpdate -> {
                previous
            }
            is RoundOverDialogDismissedResult -> previous
            is HandleNextCardResult -> {
                val newState = when (result) {
                    is HandleNextCardResult.InProgress -> {
                        previous.copy(inProgress = true)
                    }
                    is HandleNextCardResult.NewCard -> {
                        previous.copy(
                            inProgress = false
                        )
                    }
                    is HandleNextCardResult.RoundOver -> previous.copy(inProgress = false)
                    is HandleNextCardResult.GameOver -> previous
                }
                doInDebug {
                    previous.printDiff(newState)
                }
                newState
            }
            is BackPressedResult.ShowLeaveGameConfirmation -> previous.copy(showLeaveGameConfirmation = result.showDialog)
            is EndTurnPressedResult.ShowLeaveGameConfirmation -> previous.copy(showEndTurnConfirmation = result.showDialog)
            is BackPressedResult.NavigateToGames -> previous.copy(navigateToGames = result.navigate)
            is NoOp -> previous
            is SetGameResult -> previous
            is NavigateToSelectTeam -> previous.copy(navigateToTeams = result.navigate)
            is ShowPlayTooltipResult -> {
                val myTurn = gamesRepository.getCurrentGameBlocking()?.host?.id == authenticator.me?.id
                previous.copy(showPlayTooltip = myTurn && result.show)
            }

            // Handled as Triggers:
            is ShowAllCardsResult -> previous
            is StartedGameResult -> previous.copy(inProgress = false)
            is ShowCardInfoResult -> previous
        }
    }

    private fun Observable<UiEvent>.toResult(): Observable<Result> =
        publish { o ->
            Observable.mergeArray(
                o.ofType<CorrectClick>().switchMap { onCorrectClick(it.time) },
                o.ofType<StartStopClick>().switchMap { handleStartStopClick(it.buttonState, it.time) },
                o.ofType<UiEvent.TimerEnd>().switchMap { onTimerEnd() },
                o.ofType<UiEvent.OnBackPressed>().switchMap { handleBackPressed() },
                o.ofType<UiEvent.EndTurnClick>().switchMap { handleEndTurnPressed() },
                o.ofType<UiEvent.UserApprovedEndTurn>().switchMap { onApproveEndTurn() },
                o.ofType<UiEvent.UserApprovedQuitGame>().switchMap { quitGame() },
                o.ofType<UiEvent.CardsAmountClick>().switchMap { just(ShowAllCardsResult(cardDeck)) },
                o.ofType<UiEvent.RoundOverDialogDismissed>().switchMap { just(RoundOverDialogDismissedResult) },
                o.ofType<UiEvent.CardInfoClick>().switchMap { just(ShowCardInfoResult(it.cardName)) },
                o.ofType<UiEvent.FirstRoundInstructionsDismissed>().switchMap {
                    just(
                        ShowPlayTooltipResult(true),
                        ShowPlayTooltipResult(false)
                    )
                },
                o.ofType<UiEvent.OnSwitchTeamPressed>()
                    .switchMap {
                        just(
                            NavigateToSelectTeam(true),
                            NavigateToSelectTeam(false)
                        )
                    }
            )
        }

    private fun onApproveEndTurn(): ObservableSource<out Result> =
        flipLastCard(lastCard)
            .andThen(endTurn(teamsWithPlayers))

    private fun quitGame(): Observable<BackPressedResult.NavigateToGames> {
        return flipLastCard(lastCard)
            .andThen(endTurn(teamsWithPlayers)).switchMap {
                just(
                    BackPressedResult.NavigateToGames(true),
                    BackPressedResult.NavigateToGames(false)
                )
            }
    }

    private fun onCorrectClick(time: Long): Observable<out Result> =
        lastCard?.let { card ->
            if (time > 100L) {
                authenticator.me?.team?.let { teamName ->
                    merge(
                        just(HandleNextCardResult.InProgress),
                        handleCorrectCard(card, teamName)
                            .switchMap { handleNextCardWrap(time) }
                    )
                }
            } else just(NoOp)
        } ?: just(NoOp)


    private fun handleStartStopClick(
        buttonState: ButtonState,
        time: Long
    ) =
        when (buttonState) {
            ButtonState.Stopped -> startGame(authenticator.me!!)
                .switchMap {
                    handleNextCardWrap(time)
                        .map {
                            if (it is HandleNextCardResult.NewCard) {
                                StartedGameResult
                            } else {
                                it
                            }
                        }
                }
            ButtonState.Running -> pauseTurn(time)
            ButtonState.Paused -> {
                gamesRepository.getCurrentGame().toObservable().switchMap { game ->
                    if (game.round.state == RoundState.New) {
                        startRound()
                            .switchMap {
                                // if there is less than a second, end turn cause it gets tricky when the timerEnd comes before the handleNextCard resul
                                if (time < 1000L) {
                                    endTurn(teamsWithPlayers)
                                } else {
                                    resumeTurn(time)
                                        .switchMap { handleNextCardWrap(time) }
                                }
                            }
                    } else {
                        resumeTurn(time)
                    }
                }
            }
            ButtonState.Finished -> just(NoOp)
        }

    private fun onTimerEnd(): Observable<out Result> {
        Timber.i("onTimerEnd")
        return gamesRepository.getCurrentGame().toObservable().switchMap { game ->
            if (authenticator.isMyselfActivePlayerBlocking(game)) {
                audioPlayer.play("ding_clean")
                flipLastCard(lastCard)
                    .andThen(endTurn(teamsWithPlayers))
            } else {
                just(NoOp)
            }

        }
    }

    private fun handleNextCardWrap(time: Long?) =
        handleNextCard(
            cardDeck,
            time
        )


    fun unBind() {
        disposables.clear()
    }
}
