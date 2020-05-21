package com.gggames.celebs.features.players.data

import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

interface PlayersDataSource {
    fun getAllPlayers(gameId: String): Observable<List<Player>>

    fun addPlayer(gameId: String, player: Player): Completable

    fun chooseTeam(gameId: String, player: Player, teamName: String): Completable
}