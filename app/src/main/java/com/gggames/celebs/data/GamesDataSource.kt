package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(): Single<List<Game>>
}