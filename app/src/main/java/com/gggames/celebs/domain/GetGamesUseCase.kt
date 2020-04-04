package com.gggames.celebs.domain

import com.gggames.celebs.data.GamesRepository

class GetGamesUseCase(
    private val gamesRepository: GamesRepository
){
    operator fun invoke() = gamesRepository.getGames()
}