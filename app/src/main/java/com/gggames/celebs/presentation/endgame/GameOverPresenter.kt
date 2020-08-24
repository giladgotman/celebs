package com.gggames.celebs.presentation.endgame

import com.gggames.celebs.features.cards.domain.ObserveAllCards
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameType
import com.gggames.celebs.presentation.endgame.GameOverScreenContract.*
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
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
) : Presenter {

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
        // TODO: 21.08.20 use gameId to fetch game and its cards

        val results = events.publish {
            merge(
                handleUiEvent(it),
                getCardsAndGame(),
                startKonffeti()
            )
        }.doOnNext { Timber.w("RESULT:: $it") }

        val states = results.scan(State.initialValue, reduce())

        states.distinctUntilChanged()
            .compose(scheduler.applyDefault())
            .doOnNext { Timber.w("STATE:: $it") }
            .subscribe { _states.onNext(it) }
            .let { disposables.add(it) }

        results
            .distinctUntilChanged()
            .compose(scheduler.applyDefault())
            .subscribe {
                val trigger = when (it) {
                    is Result.GameAndCardsResult -> null
                    is Result.GameCleared -> Trigger.NavigateToGames
                    is Result.StartKonffetiResult -> Trigger.StartKonffeti
                    is Result.CardPressedResult -> Trigger.ShowVideoAndKonffeti(it.card, it.playerView, it.giftText)
                }
                if (trigger != null) {
                    _triggers.onNext(trigger)
                }
            }.let { disposables.add(it) }
    }

    private fun   startKonffeti(): Observable<Result.StartKonffetiResult> {

        return just(Result.StartKonffetiResult)
    }

    private fun reduce() = { previous: State, result: Result ->
        when (result) {
            is Result.GameAndCardsResult -> {
                val bonusCard = Card(
                    "bonus", "בונוס", "giftGenerator",
                    videoUrlFull = "https://drive.google.com/uc?export=download&id=1oW1niLvP52coER36I8PJPX92p-GY3818",
                    index = 27
                )

                val bonusCard2 = Card(
                    "bonus2", "בונוס 2", "giftGenerator",
                    videoUrlFull = "https://drive.google.com/uc?export=download&id=15qMFIJlUYIsRB-rh8giDYc1E2ikGC_tm",
                    index = 28
                )

                val bonusCard3 = Card(
                    "bonus3", "בונוס 3", "giftGenerator",
                    videoUrlFull = "https://drive.google.com/uc?export=download&id=1zoKIbpgBKMllmZynQi6NMCrb7MrE4sVP",
                    index = 29
                )

                val bonusCard4 = Card(
                    "bonus4", "בונוס אחרון", "giftGenerator",
                    videoUrlFull = "https://drive.google.com/uc?export=download&id=1CNw1p6wRH-DXVV6R9b-Hl2zOr140SHqq",
                    index = 30
                )

                previous.copy(
                    winningTeam = result.game.winningTeam?.name ?: "",
                    teams = result.game.teams.sortedByDescending { it.score },
                    cards = result.cards.toMutableList().also {
                        it.add(bonusCard)
                        it.add(bonusCard2)
                        it.add(bonusCard3)
                        it.add(bonusCard4)
                    },
                    mainTitle = if (result.game.type == GameType.Gift) "מזל טוב אבא!" else null,
                    subTitle = if (result.game.type == GameType.Gift) "" else null
                )
            }
            is Result.GameCleared -> previous
            is Result.StartKonffetiResult -> previous
            is Result.CardPressedResult -> previous
        }
    }

    private fun handleUiEvent(events: Observable<UiEvent>): Observable<Result> =

        merge(
        events.filter { it is UiEvent.PressedFinish }
            .flatMap { just(Result.GameCleared) },
            events.filter { it is UiEvent.PressedCard }
                .cast(UiEvent.PressedCard::class.java)
                .flatMap { just(Result.CardPressedResult(it.card, it.playerView, it.giftText)) }
            )


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
