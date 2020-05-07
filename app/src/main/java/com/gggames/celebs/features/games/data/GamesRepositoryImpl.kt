package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameStateE
import io.reactivex.Completable
import io.reactivex.Observable

class GamesRepositoryImpl(
    private val firebaseGamesDataSource: GamesDataSource
) : GamesRepository {
    override fun getGames(statesQuery: List<GameStateE>): Observable<List<Game>> {
        return firebaseGamesDataSource.getGames(statesQuery).toObservable()
    }

    override fun addGame(game: Game): Completable =
        firebaseGamesDataSource.addGame(game)

    override fun observeGame(gameId: String): Observable<Game> =
        firebaseGamesDataSource.observeGame(gameId)
}