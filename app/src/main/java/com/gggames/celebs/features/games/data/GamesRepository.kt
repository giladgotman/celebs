package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameStateE
import io.reactivex.Completable
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(statesQuery: List<GameStateE>): Observable<List<Game>>

    fun addGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<Game>
}
