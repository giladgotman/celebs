package com.gggames.celebs.data.games

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(statesQuery: List<GameState>): Observable<List<Game>>

    fun addGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<Game>
}
