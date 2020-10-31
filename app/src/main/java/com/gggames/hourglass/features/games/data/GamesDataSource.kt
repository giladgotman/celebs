package com.gggames.hourglass.features.games.data

import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(gameIds: List<String>, states: List<GameState> = emptyList()): Single<List<Game>>

    fun setGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<Game>
}
