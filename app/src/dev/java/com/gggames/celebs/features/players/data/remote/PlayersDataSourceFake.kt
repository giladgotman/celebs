package com.gggames.celebs.features.players.data.remote

import com.gggames.celebs.features.players.data.PlayersDataSource
import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class PlayersDataSourceFake @Inject constructor() : PlayersDataSource {
    override fun getAllPlayers(gameId: String): Observable<List<Player>> {
        TODO("Not yet implemented")
    }

    override fun getPlayer(gameId: String, userId: String): Observable<Player> {
        TODO("Not yet implemented")
    }

    override fun addPlayer(gameId: String, player: Player): Completable {
        TODO("Not yet implemented")
    }

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable {
        TODO("Not yet implemented")
    }

    override fun removePlayer(gameId: String, player: Player): Completable {
        TODO("Not yet implemented")
    }
}