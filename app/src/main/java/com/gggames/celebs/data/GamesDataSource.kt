package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game

interface GamesDataSource {
    fun getGames(): List<Game>
}