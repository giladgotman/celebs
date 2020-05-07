package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameStateE
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(statesQuery: List<GameStateE> = emptyList()): Single<List<Game>>

    fun addGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<Game>
}