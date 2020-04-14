package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

interface GamesRepository {
    fun getGames(): Observable<List<Game>>

    fun addGame(game: Game): Completable

    fun chooseTeam(gameId: String, player: Player, teamName: String): Completable
}
