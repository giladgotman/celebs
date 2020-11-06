package com.gggames.hourglass.features.games.data.remote

import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.model.*
import io.reactivex.Completable
import io.reactivex.Completable.fromCallable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class GamesDataSourceFake @Inject constructor() : RemoteGamesDataSource {
    val fakeGame = createGame(teams = listOf(createTeam("Team1"), createTeam("Team2")))
    var games = mutableListOf<Game>(fakeGame)
    private val gamesSubject = PublishSubject.create<Game>()

    override fun getGames(gameIds: List<String>, states: List<GameState>): Single<List<Game>> {
        Timber.w("ggg getGames: size: ${gameIds.size}")
        return Single.just(games.filter { (it.id in gameIds && ((it.state in states) || it.type == GameType.Gift)) || it.id == "id" })
    }

    override fun setGame(game: Game): Completable =
        fromCallable {
            Timber.w("ggg setGame: id: ${game.id}")
            games.indexOfFirst { it.id == game.id }.takeIf { it != -1 }?.let { index ->
                games.set(index, game)
            } ?: games.add(game)
            gamesSubject.onNext(game)
        }

    override fun observeGame(gameId: String): Observable<GameResult> {
        Timber.w("ggg observeGame, id: $gameId")
        val first = games.find { it.id == gameId }?.let {
            just(it)
        } ?: Observable.empty<Game>()
        return merge(gamesSubject.filter { it.id == gameId }, first)
            .distinctUntilChanged()
            .map { GameResult.Found(it) }
    }
}

fun createGame(
    id: String = "id",
    name: String = "Sunday fun",
    createdAt: Long = 0,
    password: String? = null,
    celebsCount: Int = 6,
    teams: List<Team> = emptyList(),
    state: GameState? = null,
    gameInfo: GameInfo = GameInfo(),
    host: Player = Player("$id.player", "$id.name"),
    type: GameType = GameType.Normal
) = Game(
    id,
    name,
    createdAt,
    password,
    celebsCount,
    teams,
    state,
    gameInfo,
    host,
    type
)

fun createTeam(
    name: String = "Team1",
    players: List<Player> = emptyList(),
    score: Int = 0,
    lastPlayerId: String? = null
) = Team(
    name = name,
    players = players,
    score = score,
    lastPlayerId = lastPlayerId
)