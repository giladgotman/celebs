package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import io.reactivex.Completable
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(): Observable<List<Game>>

    fun addGame(game: Game): Completable
}
