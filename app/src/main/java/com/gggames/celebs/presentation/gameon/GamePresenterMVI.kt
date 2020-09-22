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
import com.gggames.celebs.presentation.gameon.GameScreenContract.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.NoOp
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.Found
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.NoCardsLeft
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.*
import com.gggames.celebs.utils.media.AudioPlayer
import com.gggames.celebs.utils.rx.ofType
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
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
    private val leaveGame: LeaveGame,
    private val audioPlayer: AudioPlayer,
    private val schedulerProvider: BaseSchedulerProvider
) {
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

        val shared = events.share()

        shared.toResult()
            .subscribeOn(schedulerProvider.io())
            .doOnNext { Timber.d("RESULT:: $it") }
            .scan(GameScreenContract.State.initialState, reduce())
            .distinctUntilChanged()
            .doOnNext { Timber.d("STATE:: $it") }
            .observeOn(schedulerProvider.ui())
            .subscribe({
                _states.onNext(it)
            }) { Timber.e("Unhandled exception in the main stream") }
            .let { disposables.add(it) }

        observeGame(gameId)
            .distinctUntilChanged()

        playersObservable(gameId)
            .distinctUntilChanged()

        cardsObservable()
    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is NoCardsLeft -> previous
            is Found -> previous.copy(currentCard = result.card)
            is Result.CardsUpdateResult -> previous
            is NoOp -> previous
        }
    }

    private fun Observable<UiEvent>.toResult(): Observable<Result> =
        publish { o ->
            Observable.mergeArray(
                o.ofType<CorrectClick>().flatMap { pickNextCardNew() },
                o.ofType<StartStopClick>().flatMap { just(NoOp) }
            )
        }

    private fun pickNextCardNew(): Observable<PickNextCardResult> {
        return Observable.fromCallable {
            pickNextCard()?.let {
                Found(it)
            } ?: NoCardsLeft(game.round, null)
        }
    }

    private fun pickNextCard(): Card? {
        return null
    }

    fun unBind() {
        disposables.clear()
    }


}
