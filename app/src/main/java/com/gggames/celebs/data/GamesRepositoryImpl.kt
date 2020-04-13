package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

class GamesRepositoryImpl(
    private val firebaseGamesDataSource: GamesDataSource
) : GamesRepository{
    override fun getGames(): Observable<List<Game>> {
        return firebaseGamesDataSource.getGames().toObservable()
    }

    override fun addGame(game: Game): Completable =
        firebaseGamesDataSource.addGame(game)


    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable =
        firebaseGamesDataSource.chooseTeam(gameId, player, teamName)
}