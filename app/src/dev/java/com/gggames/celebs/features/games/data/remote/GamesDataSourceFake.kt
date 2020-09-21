package com.gggames.celebs.features.games.data.remote

import com.gggames.celebs.features.games.data.GamesDataSource
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import com.gggames.celebs.model.GameType
import io.reactivex.Completable
import io.reactivex.Completable.fromCallable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class GamesDataSourceFake @Inject constructor() : GamesDataSource {

    var games = mutableListOf<Game>()
    val gamesSubject = PublishSubject.create<Game>()

    override fun getGames(gameIds: List<String>, states: List<GameState>): Single<List<Game>> {
        Timber.w("ggg getGames: size: ${gameIds.size}")
        return Single.just(games.filter { (it.id in gameIds && ((it.state in states) || it.type == GameType.Gift)) })
    }

    override fun setGame(game: Game): Completable =
        fromCallable {
            Timber.w("ggg setGame: id: ${game.id}")
            games.indexOfFirst { it.id == game.id }.takeIf { it != -1 }?.let { index ->
                games.set(index, game)
            } ?: games.add(game)
            gamesSubject.onNext(game)
        }

    override fun observeGame(gameId: String): Observable<Game> {
        Timber.w("ggg observeGame, id: $gameId")
        val first = games.find { it.id == gameId }?.let {
            just(it)
        } ?: Observable.empty<Game>()
        return merge(gamesSubject.filter { it.id == gameId }, first)
    }
}
