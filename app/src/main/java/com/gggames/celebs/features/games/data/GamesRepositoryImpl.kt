package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class GamesRepositoryImpl @Inject constructor(
    private val gamesDataSource: GamesDataSource
) : GamesRepository {
    override fun getGames(statesQuery: List<GameState>): Observable<List<Game>> {
        return gamesDataSource.getGames(statesQuery).toObservable()
    }

    override fun setGame(game: Game): Completable =
        gamesDataSource.setGame(game)

    override fun observeGame(gameId: String): Observable<Game> =
        gamesDataSource.observeGame(gameId)
}