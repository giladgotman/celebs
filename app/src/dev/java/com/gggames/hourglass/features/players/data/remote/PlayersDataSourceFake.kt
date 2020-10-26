package com.gggames.hourglass.features.players.data.remote

import com.gggames.hourglass.features.players.data.PlayersDataSource
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.PlayerTurnState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PlayersDataSourceFake @Inject constructor() : PlayersDataSource {

    private val players = mutableListOf<Player>(
        createPlayer(
            id = "fake1",
            name = "fake1",
            team = "Team1"
        ),
        createPlayer(
            id = "fake2",
            name = "fake2",
            team = "Team2"
        )
    )
    private val playersSubject = PublishSubject.create<List<Player>>()


    override fun getAllPlayers(gameId: String): Observable<List<Player>> =
        merge(just(players), playersSubject)

    override fun getPlayer(gameId: String, userId: String): Observable<Player> =
        merge(
            just(players.find { it.id == userId }),
            playersSubject.map { it.find { it.id == userId } }
        )


    override fun addPlayer(gameId: String, player: Player): Completable =
        Completable.fromCallable {
            players.add(player)
            playersSubject.onNext(players)
        }

    override fun chooseTeam(
        gameId: String,
        player: Player,
        teamName: String
    ): Completable =
        Completable.fromCallable {
            players.indexOfFirst { it.id == player.id }.takeIf { it != -1 }?.let { index ->
                players.set(index, player.copy(team = teamName))
                playersSubject.onNext(players)
            }
        }

    override fun removePlayer(gameId: String, player: Player): Completable =
        Completable.fromCallable {
            players.indexOfFirst { it.id == player.id }.takeIf { it != -1 }?.let { index ->
                players.removeAt(index)
                playersSubject.onNext(players)
            }
        }
}


fun createPlayer(
    id: String = "id",
    name: String = "name",
    team: String? = null,
    games: List<String> = emptyList(),
    playerTurnState: PlayerTurnState = PlayerTurnState.Idle
): Player = Player(
    id = id,
    name = name,
    team = team,
    games = games,
    playerTurnState = playerTurnState
)