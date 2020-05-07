package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.GameStateE

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