package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(): Observable<List<Game>>
}
