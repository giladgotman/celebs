package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.gameon.*
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.CorrectClick
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.StartStopClick
import com.gggames.celebs.utils.media.AudioPlayer
import com.gggames.celebs.utils.rx.ofType
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject


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
    private val endTurn: EndTurn,
    private val flipLastCard: FlipLastCard,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
    private var cardDeck = listOf<Card>()

    private var lastCard: Card? = null
    private val disposables = CompositeDisposable()

    private val game: Game
        get() = gamesRepository.currentGame!!

    private var lastGame: Game? = null

    private val _states = PublishSubject.create<State>()
    val states: Observable<State> = _states

    fun bind(events: Observable<UiEvent>) {
        val gameId = game.id

        val uiEvent = events
            .doOnNext { Timber.d("USER:: $it") }

        val dataInput = merge(
            observeGame(gameId),
            playersObservable(gameId),
            cardsObservable()
        )

        val allInput = merge(uiEvent.toResult(), dataInput)

        allInput
            .subscribeOn(schedulerProvider.io())
            .doOnNext { Timber.d("RESULT:: $it") }
            .share()
            .scan(State.initialState, reduce())
            .distinctUntilChanged()
            .doOnNext { Timber.d("STATE:: \n$it") }
            .observeOn(schedulerProvider.ui())
            .subscribe({
                _states.onNext(it)
            }) { Timber.e("Unhandled exception in the main stream") }
            .let { disposables.add(it) }

    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is GameUpdate -> {
                val meActive = authenticator.isMyselfActivePlayerBlocking(game)
                val turnState = result.game.gameInfo.round.turn.state
                val turnOver = result.game.turn.state == TurnState.Over &&
                        result.game.round.state == RoundState.Started
                val roundOver = result.game.round.state == RoundState.Ended && previous.round.state != RoundState.Ended
                val newState = previous.copy(
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
                    showGameOver = game.state == GameState.Finished,
                    currentCard = game.turn.currentCard,
                    currentPlayer = game.turn.player,
                    revealCurrentCard = meActive,
                    correctButtonEnabled = meActive && turnState == TurnState.Running,
                    lastPlayer = game.currentPlayer ?: previous.lastPlayer,
                    cardsFoundInTurn = cardDeck.filter { it.id in result.game.turn.cardsFound }
                )
                lastGame = result.game
                lastCard = result.game.turn.currentCard

                newState
            }
            is PlayersUpdate -> {
                val updatedTeams = previous.teamsWithScore.map { team ->
                    team.copy(players = result.players.filter { it.team == team.name })
                }
                previous.copy(teamsWithPlayers = updatedTeams)
            }
            is CardsUpdate -> {
                cardDeck = result.cards
                previous.copy(
                    cardsInDeck = result.cards.filter { !it.used }.size,
                    totalCardsInGame = result.cards.size
                )
            }
            is RoundOverDialogDismissedResult -> previous
            is HandleNextCardResult -> {
                if (result is HandleNextCardResult.InProgress) {
                    previous.copy(inProgress = true)
                } else {
                    previous.copy(inProgress = false)
                }
            }
            is BackPressedResult.ShowLeaveGameConfirmation -> previous.copy(showLeaveGameConfirmation = result.showDialog)
            is BackPressedResult.NavigateToGames -> previous.copy(navigateToGames = result.navigate)
            is NoOp -> previous
            is SetGameResult -> previous
        }
    }

    private fun Observable<UiEvent>.toResult(): Observable<Result> =
        publish { o ->
            Observable.mergeArray(
                o.ofType<CorrectClick>().switchMap { onCorrectClick(it.time) },
                o.ofType<StartStopClick>().switchMap { handleStartStopClick(it.buttonState, it.time) },
                o.ofType<UiEvent.TimerEnd>().switchMap { onTimerEnd() },
                o.ofType<UiEvent.OnBackPressed>().switchMap { handleBackPressed(game) },
                o.ofType<UiEvent.UserApprovedQuitGame>().switchMap { quitGame() },
                o.ofType<UiEvent.RoundOverDialogDismissed>().switchMap { just(RoundOverDialogDismissedResult) }
            )
        }

    private fun quitGame(): Observable<BackPressedResult.NavigateToGames> {
        return endTurn(game).switchMap {
            just(
                BackPressedResult.NavigateToGames(true),
                BackPressedResult.NavigateToGames(false)
            )
        }
    }

    private fun onCorrectClick(time: Long): Observable<out Result> =
        lastCard?.let { card ->
            authenticator.me?.team?.let { teamName ->
                // TODO: 09.10.20 check if the InProgress can be removed cause the SetGame will start with InProgress
                merge(
                    just(HandleNextCardResult.InProgress),
                    handleCorrectCard(card, game, teamName)
                        .switchMap { handleNextCardWrap(time) }
                )
            }
        } ?: just(NoOp)


    private fun handleStartStopClick(
        buttonState: ButtonState,
        time: Long?
    ) =
        when (buttonState) {
            ButtonState.Stopped -> startGame(authenticator.me!!, game)
                .switchMap { handleNextCardWrap(time) }
            ButtonState.Running -> pauseTurn(game)
            ButtonState.Paused -> {
                if (game.round.state == RoundState.New) {
                    startRound(game)
                        .switchMap { handleNextCardWrap(time) }
                        .switchMap { resumeTurn(game) }
                } else {
                    resumeTurn(game)
                }
            }
            ButtonState.Finished -> just(NoOp)
        }

    private fun onTimerEnd(): Observable<out Result> {
        return if (authenticator.isMyselfActivePlayerBlocking(game)) {
            audioPlayer.play("timesupyalabye")
            flipLastCard(lastCard)
                .andThen(endTurn(game))
        } else {
            just(NoOp)
        }

    }

    private fun handleNextCardWrap(time: Long?) =
        handleNextCard(
            cardDeck,
            game,
            time
        )


    fun unBind() {
        disposables.clear()
    }
}
