package com.gggames.celebs.features.players.data

import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class PlayersRepositoryImpl @Inject constructor(
    private val firebasePlayersDataSource: PlayersDataSource
) : PlayersRepository {

    override var currentPlayers: List<Player> = emptyList()

    override fun getAllPlayers(gameId: String): Observable<List<Player>> =
        firebasePlayersDataSource.getAllPlayers(gameId).doOnNext { currentPlayers = it }

    override fun addPlayer(gameId: String, player: Player): Completable =
        firebasePlayersDataSource.addPlayer(gameId, player)

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable =
        firebasePlayersDataSource.chooseTeam(gameId, player, teamName)

    override fun removePlayer(gameId: String, player: Player): Completable =
        firebasePlayersDataSource.removePlayer(gameId, player)
}