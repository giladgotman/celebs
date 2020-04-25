package com.gggames.celebs.domain.games

import com.gggames.celebs.data.games.GamesRepository
import com.gggames.celebs.data.model.GameInfo
import com.gggames.celebs.data.model.GameState

/*
Gets games that are Empty, Ready or Started
 */
class GetGames(
    private val gamesRepository: GamesRepository
){
    operator fun invoke() = gamesRepository.getGames(
        listOf(GameState.Empty, GameState.Ready(GameInfo()), GameState.Started(GameInfo()))
    )
}