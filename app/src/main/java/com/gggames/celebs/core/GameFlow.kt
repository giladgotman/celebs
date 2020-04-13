package com.gggames.celebs.core

import com.gggames.celebs.data.model.Game

object GameFlow {

    var currentGame: Game? = null
    private set

    fun joinGame(game: Game) {
        currentGame = game
    }

}