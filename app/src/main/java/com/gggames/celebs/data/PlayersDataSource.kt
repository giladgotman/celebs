package com.gggames.celebs.data

import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Single

interface PlayersDataSource {
    fun getAllPlayers(gameId: String): Single<List<Player>>

    fun addPlayer(gameId: String, player: Player): Completable
}