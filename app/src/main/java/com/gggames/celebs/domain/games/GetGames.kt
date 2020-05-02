package com.gggames.celebs.domain.games

import com.gggames.celebs.data.games.GamesRepository
import com.gggames.celebs.data.model.GameStateE

/*
Gets games that are Empty, Ready or Started
 */
class GetGames(
    private val gamesRepository: GamesRepository
){
    operator fun invoke() = gamesRepository.getGames(
        listOf(GameStateE.Created, GameStateE.Started)
    )
}