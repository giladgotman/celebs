package com.gggames.hourglass.features.games.data

import com.gggames.hourglass.features.games.data.memory.InMemoryGamesDataSource
import com.gggames.hourglass.features.games.data.remote.RemoteGamesDataSource
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.merge
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteGamesDataSource,
    private val inMemoryDataSource: InMemoryGamesDataSource
) : GamesRepository {

    override fun getGames(gameIds: List<String>, states: List<GameState>): Observable<List<Game>> {
        return remoteDataSource.getGames(gameIds, states).toObservable()
    }

    override fun setGame(game: Game?, updateRemote: Boolean): Completable {
        val setInMemory = game?.let {
            inMemoryDataSource.setGame(it)
        } ?: inMemoryDataSource.clearCache()
        Timber.w("setGame, currentGame: $game")
        return setInMemory.andThen(
            if (updateRemote && game != null) {
                remoteDataSource.setGame(game)
            } else {
                Completable.complete()
            }
        )
    }

    override fun observeGame(gameId: String): Observable<GameResult> {
        val fetchAndCache = remoteDataSource.observeGame(gameId)
            .doOnNext {
                if (it is GameResult.Found) {
                    Timber.i("observeGame REMOTE onNext: \n $it")
                    inMemoryDataSource.setGame(it.game)
                }
            }

        return merge(
            inMemoryDataSource.observeGame(gameId)
                .doOnNext {
                    Timber.i("observeGame MEMRY onNext: \n $it")
                },
            fetchAndCache
        )
            .distinctUntilChanged()
            .doOnNext {
                Timber.i("observeGame MERGE onNext: \n $it")
            }
    }


    override fun getCurrentGame() = inMemoryDataSource.getCurrentGame().map {
        if (it is GameResult.Found) {
            it.game
        } else {
            throw IllegalStateException("No Current game is found in cache")
        }
    }

    override fun getCurrentGameBlocking(): Game? = inMemoryDataSource.getCurrentGameBlocking()
}

