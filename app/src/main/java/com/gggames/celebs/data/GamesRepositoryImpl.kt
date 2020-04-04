package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import io.reactivex.Observable

class GamesRepositoryImpl(
    private val firebaseGamesDataSource: GamesDataSource
) : GamesRepository{
    override fun getGames(): Observable<List<Game>> {
        return firebaseGamesDataSource.getGames().toObservable()
    }

}