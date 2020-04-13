package com.gggames.celebs.data

import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
import io.reactivex.Completable
import io.reactivex.Single

interface GamesDataSource {
    fun getGames(): Single<List<Game>>

    fun addGame(game: Game): Completable

    fun chooseTeam(gameId: String, player: Player, teamName: String): Completable
}