package com.gggames.hourglass.features.games.data.memory

import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.features.games.data.GameResult.Found
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

interface InMemoryGamesDataSource {
    fun getGames(gameIds: List<String>, states: List<GameState> = emptyList()): Single<List<Game>>

    fun setGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<GameResult>

    fun getCurrentGame(): Single<GameResult>

    fun getCurrentGameBlocking(): Game?

    fun clearCache(): Completable
}

class GamesMemoryDataSource @Inject constructor() : InMemoryGamesDataSource {

    private val currentGame = PublishSubject.create<GameResult>()

    private val fakeDelay = 800L

    private var currentGameCache: GameResult by Delegates.observable<GameResult>(GameResult.NotFound) { _, _, newValue ->
        currentGame.onNext(newValue)
    }


    override fun getGames(gameIds: List<String>, states: List<GameState>): Single<List<Game>> {
        TODO("Not yet implemented")
    }

    override fun setGame(game: Game): Completable =
        Observable.timer(fakeDelay, TimeUnit.MILLISECONDS)
            .flatMapCompletable {
                currentGameCache = Found(game)
                complete()
            }

    override fun observeGame(gameId: String): Observable<GameResult> =
        merge(
            just(currentGameCache),
            currentGame
        ).map { gameResult ->
            if (gameResult is Found && gameResult.game.id == gameId) Found(gameResult.game) else GameResult.NotFound
        }
            .distinctUntilChanged()


    override fun getCurrentGame() = Single.just(currentGameCache)

    override fun getCurrentGameBlocking(): Game? =
        when (currentGameCache) {
            is Found -> (currentGameCache as Found).game
            else -> null
        }


    override fun clearCache() =
        Completable.fromCallable {
            currentGameCache = GameResult.NotFound
            complete()
        }
}