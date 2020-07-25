package com.gggames.celebs.features.games.data.remote

import com.gggames.celebs.common.factory.createGame
import com.gggames.celebs.features.games.data.GamesDataSource
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class FirebaseGamesDataSourceFake @Inject constructor(): GamesDataSource{
    override fun getGames(gameIds: List<String>, states: List<GameState>): Single<List<Game>> {
        Timber.w("ggg getGames: size: ${gameIds.size}")
        return Single.just(emptyList())
    }

    override fun setGame(game: Game): Completable {
        Timber.w("ggg setGame: id: ${game.id}")
        return complete()
    }

    override fun observeGame(gameId: String): Observable<Game> {
        Timber.w("ggg observeGame, id: $gameId")
        return just(createGame(id = gameId))
    }
}