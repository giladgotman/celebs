package com.gggames.celebs.features.players.data

import com.gggames.celebs.features.players.data.PlayersDataSource
import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

class PlayersRepositoryImpl(
    private val firebasePlayersDataSource: PlayersDataSource
) : PlayersRepository {

    override fun getAllPlayers(gameId: String): Observable<List<Player>> =
        firebasePlayersDataSource.getAllPlayers(gameId)

    override fun addPlayer(gameId: String, player: Player): Completable =
        firebasePlayersDataSource.addPlayer(gameId, player)

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable =
        firebasePlayersDataSource.chooseTeam(gameId, player, teamName)

}