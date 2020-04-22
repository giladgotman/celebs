package com.gggames.celebs.domain.games

import com.gggames.celebs.data.games.GamesRepository

class GetGames(
    private val gamesRepository: GamesRepository
){
    operator fun invoke() = gamesRepository.getGames()
}