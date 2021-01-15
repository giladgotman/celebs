package com.gggames.hourglass.features.games.data

import com.gggames.hourglass.features.games.data.GameResult.Found
import com.gggames.hourglass.features.games.data.memory.InMemoryGamesDataSource
import com.gggames.hourglass.features.games.data.remote.RemoteGamesDataSource
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteGamesDataSource,
    private val inMemoryDataSource: InMemoryGamesDataSource,
    private val schedulerProvider: BaseSchedulerProvider
) : GamesRepository {

    private val disposables = CompositeDisposable()

    override fun getGames(gameIds: List<String>, states: List<GameState>): Observable<List<Game>> {
        return remoteDataSource.getGames(gameIds, states).toObservable()
    }

    override fun setGame(game: Game?, updateRemote: Boolean): Completable {
        Timber.w(":: setGame, currentCard: ${game?.round?.turn?.currentCard}, ts: ${game?.timestamp}")
        val setInMemory = game?.let {
            inMemoryDataSource.setGame(it)
        } ?: inMemoryDataSource.clearCache()

        if (updateRemote && game != null) {
            remoteDataSource.setGame(game)
                .compose(schedulerProvider.applyCompletableDefault())
                .subscribe({
                    Timber.i("sss setGame remote done")
                }, {
                    Timber.e(it,"sss setGame remote error")
                }).let { disposables.add(it) }
        }

        return setInMemory
    }

    // TODO: 11.01.21 consider chaining together and don't wait for firebase success
//    override fun setGame(game: Game?, updateRemote: Boolean): Completable {
//        val setInMemory = game?.let {
//            inMemoryDataSource.setGame(it)
//        } ?: inMemoryDataSource.clearCache()
//
//        val setRemote = if (updateRemote && game != null) {
//            remoteDataSource.setGame(game)
//                .compose(schedulerProvider.applyCompletableDefault())
//        } else Completable.complete()
//
//        return setInMemory.andThen(setRemote)
//    }

    override fun observeGame(gameId: String): Observable<GameResult> {
        val fetchAndCache: Completable = remoteDataSource.observeGame(gameId)
            .filter { it is Found }
            .cast(Found::class.java)
            .switchMapCompletable {
                Timber.i("observeGame REMOTE onNext: \n $it")
                val inMemGameTimestamp = inMemoryDataSource.getCurrentGameBlocking()?.timestamp ?: 0
                if (it.game.timestamp >= inMemGameTimestamp) {
                    inMemoryDataSource.setGame(it.game)
                } else {
                    Timber.i("observeGame ignoring game, timestamps: r:${it.game.timestamp},  m:$inMemGameTimestamp")
                    complete()
                }

            }

        return merge(
            inMemoryDataSource.observeGame(gameId)
                .doOnNext {
                    Timber.i("observeGame MEMRY onNext: \n $it")
                },
            fetchAndCache.toObservable()
        )
            .distinctUntilChanged()
            .doOnNext {
                Timber.i("observeGame MERGE onNext: \n $it")
            }
    }


    override fun getCurrentGame() = inMemoryDataSource.getCurrentGame().map {
        if (it is Found) {
            it.game
        } else {
            throw IllegalStateException("No Current game is found in cache")
        }
    }

    override fun getCurrentGameBlocking(): Game? = inMemoryDataSource.getCurrentGameBlocking()
}

