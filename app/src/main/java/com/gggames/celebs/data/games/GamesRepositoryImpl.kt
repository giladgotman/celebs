package com.gggames.celebs.data.games

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.GameState
import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

class GamesRepositoryImpl(
    private val firebaseGamesDataSource: GamesDataSource
) : GamesRepository {
    override fun getGames(statesQuery: List<GameState>): Observable<List<Game>> {
        return firebaseGamesDataSource.getGames(statesQuery).toObservable()
    }

    override fun addGame(game: Game): Completable =
        firebaseGamesDataSource.addGame(game)

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable =
        firebaseGamesDataSource.chooseTeam(gameId, player, teamName)

    override fun observeGame(gameId: String): Observable<Game> =
        firebaseGamesDataSource.observeGame(gameId)
}