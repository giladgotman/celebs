package com.gggames.celebs.data.players

import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

interface PlayersRepository {
    fun getAllPlayers(gameId: String): Observable<List<Player>>

    fun addPlayer(gameId: String, player: Player): Completable

    fun chooseTeam(gameId: String, player: Player, teamName: String): Completable
}
