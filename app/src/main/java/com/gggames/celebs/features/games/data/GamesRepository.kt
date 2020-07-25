package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(gameIds: List<String>, states: List<GameState>): Observable<List<Game>>

    fun setGame(game: Game?, updateRemote: Boolean = true): Completable

    fun observeGame(gameId: String): Observable<Game>

    var currentGame: Game?
}

const val MAX_CARDS = 6
