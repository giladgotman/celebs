package com.gggames.celebs.presentation.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.gameon.PickNextCard
import com.gggames.celebs.features.gameon.StartGame
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.LeaveGame
import com.gggames.celebs.features.players.domain.ObservePlayers
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.RoundState
import com.gggames.celebs.presentation.gameon.GameScreenContract.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.NoOp
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.Found
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.NoCardsLeft
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.CorrectClick
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.StartStopClick
import com.gggames.celebs.utils.media.AudioPlayer
import com.gggames.celebs.utils.rx.ofType
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.ObservableSource
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
    private val pickNextCard: PickNextCard,
    private val startGame: StartGame,
    private val leaveGame: LeaveGame,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
    private var cardDeck = mutableListOf<Card>()

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
            .scan(State.initialState, reduce())
            .distinctUntilChanged()
            .doOnNext { Timber.d("STATE:: $it") }
            .observeOn(schedulerProvider.ui())
            .subscribe({
                _states.onNext(it)
            }) { Timber.e("Unhandled exception in the main stream") }
            .let { disposables.add(it) }

    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is NoCardsLeft -> previous
            is Found -> previous.copy(currentCard = result.card)
            is Result.CardsUpdateResult -> previous
            is NoOp -> previous
            is Result.GameUpdate -> {
                lastGame = result.game
                previous.copy(
                    teams = result.game.teams,
                    round = result.game.round.roundNumber.toString()

                )
            }
            is Result.PlayersUpdate -> {
                val updatedTeams = previous.teams.map { team ->
                    team.copy(players = result.players.filter { it.team == team.name })
                }
                previous.copy(teams = updatedTeams)}
            is Result.CardsUpdate -> {
                cardDeck = result.cards.toMutableList()
                previous.copy(cardsInDeck = result.cards.size)
            }
        }
    }

    private fun Observable<UiEvent>.toResult(): Observable<Result> =
        publish { o ->
            Observable.mergeArray(
                o.ofType<CorrectClick>().flatMap { pickNextCardWrap(it.time) },
                o.ofType<StartStopClick>().flatMap { handleStartStopClick(it.buttonState, it.time) },
                o.ofType<UiEvent.EndTurnClick>().flatMap { just(NoOp) }

            )
        }

    private fun handleStartStopClick(
        buttonState: ButtonState,
        time: Long?
    ) =
        when (buttonState) {
            ButtonState.Stopped -> startGame(authenticator.me!!, game)
                .andThen(pickNextCardWrap(time))
            ButtonState.Running -> pickNextCardWrap(time)
            ButtonState.Paused -> just(NoOp)
            ButtonState.Finished -> just(NoOp)
        }


    private fun pickNextCardWrap(time: Long?): ObservableSource<Result.PickNextCardResult> {
        return pickNextCard(
            cardDeck,
            game.type,
            game.round,
            time
        )
    }

    fun unBind() {
        disposables.clear()
    }
}
