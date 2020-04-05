package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import io.reactivex.Completable
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(): Single<List<Game>>

    fun addGame(game: Game): Completable
}