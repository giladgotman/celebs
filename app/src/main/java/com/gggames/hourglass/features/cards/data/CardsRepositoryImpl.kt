package com.gggames.hourglass.features.cards.data

import com.gggames.hourglass.features.cards.data.memeory.CardsLocalDataSource
import com.gggames.hourglass.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CardsRepositoryImpl @Inject constructor(
    private val localDataSource: CardsLocalDataSource,
    private val remoteDataSource: CardsDataSource,
    private val schedulerProvider: BaseSchedulerProvider

) : CardsRepository {

    private val disposables = CompositeDisposable()

    override fun getAllCards(): Observable<List<Card>> {
        val fetchAndCache: Completable = remoteDataSource.getAllCards()
            .switchMapCompletable {
                Timber.i("getAllCards REMOTE onNext: \n $it")
                localDataSource.setCards(it)
            }

        return Observable.merge(
            localDataSource.getAllCards()
                .doOnNext {
                    Timber.i("getAllCards MEMRY onNext: \n $it")
                },
            fetchAndCache.toObservable()
        )
            .distinctUntilChanged()
            .doOnNext {
                Timber.i("getAllCards MERGE onNext: \n $it")
            }
    }

    override fun addCards(cards: List<Card>): Completable =
        localDataSource.addCards(cards)
            .andThen(remoteDataSource.addCards(cards))

    override fun updateCard(card: Card): Completable {
        remoteDataSource.update(card)
            .compose(schedulerProvider.applyCompletableDefault())
            .subscribe({
                Timber.i("sss updateCard remote done")
            }, {
                Timber.e(it, "sss updateCard remote error")
            }).let { disposables.add(it) }

        return localDataSource.update(card)

    }

    override fun setCards(cards: List<Card>): Completable =
        localDataSource.setCards(cards)
            .andThen(remoteDataSource.setCards(cards))
}
