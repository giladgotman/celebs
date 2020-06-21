package com.gggames.celebs.presentation.endgame

import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.*
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class GameOverPresenter @Inject constructor(
    val gamesRepository: GamesRepository,
    val getCards: ObserveAllCards,
    val scheduler: BaseSchedulerProvider
) : Presenter{

    private val _states = PublishSubject.create<State>()
    override val states: Observable<State> = _states
    private val disposables = CompositeDisposable()

    private val _triggers = PublishSubject.create<Trigger>()
    override val triggers: Observable<Trigger> = _triggers

    override fun bind(
        events: Observable<UiEvent>,
        gameId: String
    ) {
        Timber.w("ggg bind, gameId: $gameId")
        val results = events.publish {
            merge(
                handleUiEvent(it),
                getCardsAndGame()
            )
        }

        val states = results.scan(State.initialValue, reduce())

        states.distinctUntilChanged()
            .compose(scheduler.applyDefault())
            .subscribe { _states.onNext(it) }
            .let { disposables.add(it) }

        results
            .distinctUntilChanged()
            .compose(scheduler.applyDefault())
            .subscribe {
                val trigger = when (it) {
                    is Result.GameAndCardsResult -> null
                    is Result.GameCleared -> Trigger.NavigateToGames
                }
                if (trigger != null) {
                    _triggers.onNext(trigger)
                }
            }.let { disposables.add(it) }
    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is Result.GameAndCardsResult -> previous.copy(
                winningTeam = result.game.winningTeam?.name ?: "",
                teams = result.game.teams.sortedBy { it.score },
                cards = result.cards
            )
            Result.GameCleared -> previous
        }
    }

    private fun handleUiEvent(events: Observable<UiEvent>): Observable<Result> =
        events.filter { it is UiEvent.PressedFinish }
            .flatMap { finishGame().andThen(just(Result.GameCleared)) }


    private fun finishGame(): Completable =
        Completable.complete()

    private fun getCurrentGame(): Observable<Game> = Observable.fromCallable {
        gamesRepository.currentGame!!
    }

    private fun getCardsAndGame() = combineLatest(
        getCurrentGame(),
        getCards(),
        BiFunction<Game, List<Card>, Result.GameAndCardsResult> { game, cards ->
            Result.GameAndCardsResult(
                game,
                cards
            )
        })

    override fun unBind() {
        disposables.clear()
    }

}
