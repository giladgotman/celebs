package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.gameon.EndTurn
import com.gggames.celebs.features.gameon.FlipLastCard
import com.gggames.celebs.features.gameon.HandleNextCard
import com.gggames.celebs.features.gameon.StartGame
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.LeaveGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.RoundState
import com.gggames.celebs.model.TurnState
import com.gggames.celebs.presentation.gameon.GameScreenContract.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.NoOp
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
    private val updateGame: SetGame,
    private val observeGame: ObserveGame,
    private val authenticator: Authenticator,
    private val cardsRepository: CardsRepository,
    private val gamesRepository: GamesRepository,
    private val handleNextCard: HandleNextCard,
    private val startGame: StartGame,
    private val endTurn: EndTurn,
    private val flipLastCard: FlipLastCard,
    private val leaveGame: LeaveGame,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
    private var cardDeck = listOf<Card>()

    private var lastCard: Card? = null
    private val disposables = CompositeDisposable()

    private val game: Game
        get() = gamesRepository.currentGame!!

    private var lastGame: Game? = null
    private val roundState: RoundState
        get() = game.gameInfo.round.state

    private var cardsFoundInTurn = mutableListOf<Card>()

    private val _states = PublishSubject.create<State>()
    val states: Observable<State> = _states

    fun bind(events: Observable<UiEvent>) {
        val gameId = game.id

        val uiEvent = events
            .doOnNext { Timber.d("USER:: \n$it") }

        val dataInput = merge(
            observeGame(gameId),
            playersObservable(gameId),
            cardsObservable()
        )

        val allInput = merge(uiEvent.toResult(), dataInput)

        allInput
            .subscribeOn(schedulerProvider.io())
            .doOnNext { Timber.d("RESULT:: \n$it") }
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
            is Result.CardsUpdateResult -> previous
            is NoOp -> previous
            is Result.GameUpdate -> {
                lastGame = result.game
                val meActive = authenticator.isMyselfActivePlayerBlocking(game)
                val turnState = result.game.gameInfo.round.turn.state
                previous.copy(
                    teamsWithScore = result.game.teams,
                    round = result.game.round.roundNumber.toString(),
                    isTimerRunning = turnState == TurnState.Running,
                    playButtonState = PlayButtonState(
                        isEnabled = meActive || result.game.currentPlayer == null,
                        state = turnState.toPlayButtonState()
                    ),
                    resetTime = (previous.isTimerRunning && turnState != TurnState.Running)

                )
            }
            is Result.PlayersUpdate -> {
                val updatedTeams = previous.teamsWithScore.map { team ->
                    team.copy(players = result.players.filter { it.team == team.name })
                }
                previous.copy(teamsWithPlayers = updatedTeams)
            }
            is Result.CardsUpdate -> {
                cardDeck = result.cards
                previous.copy(
                    cardsInDeck = result.cards.filter { !it.used }.size,
                    totalCardsInGame = result.cards.size
                )
            }
            is Result.HandleNextCardResult.NewCard -> previous.copy(currentCard = result.newCard)
            is Result.HandleNextCardResult.RoundOver -> previous
            is Result.HandleNextCardResult.GameOver -> previous
        }
    }

    private fun Observable<UiEvent>.toResult(): Observable<Result> =
        publish { o ->
            Observable.mergeArray(
                o.ofType<CorrectClick>().flatMap { handleNextCardWrap(it.time) },
                o.ofType<StartStopClick>().flatMap { handleStartStopClick(it.buttonState, it.time) },
                o.ofType<UiEvent.TimerEnd>().flatMap { onTimerEnd() }

            )
        }


    private fun handleStartStopClick(
        buttonState: ButtonState,
        time: Long?
    ) =
        when (buttonState) {
            ButtonState.Stopped -> startGame(authenticator.me!!, game)
                .andThen(handleNextCardWrap(time))
            ButtonState.Running -> just(NoOp)
            ButtonState.Paused -> just(NoOp)
            ButtonState.Finished -> just(NoOp)
        }

    private fun onTimerEnd(): Observable<NoOp> {
        return if (authenticator.isMyselfActivePlayerBlocking(game)) {
            audioPlayer.play("timesupyalabye")
            flipLastCard(lastCard)
                .andThen(endTurn(game))
                .andThen(just(NoOp))
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

private fun TurnState.toPlayButtonState() =
    when (this) {
        TurnState.Idle -> ButtonState.Stopped
        TurnState.Stopped -> ButtonState.Stopped
        TurnState.Running -> ButtonState.Running
        TurnState.Paused -> ButtonState.Paused
    }

