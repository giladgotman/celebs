package com.gggames.celebs.data.games

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.GameStateE
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(statesQuery: List<GameStateE> = emptyList()): Single<List<Game>>

    fun addGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<Game>
}