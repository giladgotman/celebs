package com.gggames.hourglass.features.games.data

import com.gggames.hourglass.features.games.data.GameResult.Found
import com.gggames.hourglass.features.games.data.memory.InMemoryGamesDataSource
import com.gggames.hourglass.features.games.data.remote.RemoteGamesDataSource
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.merge
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
        return setInMemory.andThen(
            if (updateRemote && game != null) {
                remoteDataSource.setGame(game)
            } else {
                Completable.complete()
            }
        )
    }

    override fun observeGame(gameId: String): Observable<GameResult> {
        val fetchAndCache: Completable = remoteDataSource.observeGame(gameId)
            .filter{ it is Found }
            .cast(Found::class.java)
            .switchMapCompletable {
                inMemoryDataSource.setGame(it.game)
            }

        return merge(
            inMemoryDataSource.observeGame(gameId)
                .doOnNext {
                },
            fetchAndCache.toObservable()
        )
            .distinctUntilChanged()
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

