package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game

class GamesRepositoryImpl(
    private val firebaseGamesDataSource: GamesDataSource
) : GamesRepository{
    override fun getGames(): List<Game> {
        return firebaseGamesDataSource.getGames()
    }

}