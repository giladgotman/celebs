package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game

interface GamesRepository {
    fun getGames(): List<Game>
}
