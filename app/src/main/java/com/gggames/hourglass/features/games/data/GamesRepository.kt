package com.gggames.hourglass.features.games.data

import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface GamesRepository {
    fun getGames(gameIds: List<String>, states: List<GameState>): Observable<List<Game>>

    fun setGame(game: Game?, updateRemote: Boolean = true): Completable

    fun observeGame(gameId: String): Observable<GameResult>

    fun getCurrentGame(): Single<Game>

//    var currentGame: Game?
}

const val MAX_CARDS = 6
