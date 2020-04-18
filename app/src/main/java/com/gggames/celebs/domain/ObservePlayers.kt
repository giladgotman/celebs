package com.gggames.celebs.domain

import com.gggames.celebs.data.PlayersRepository

class ObservePlayers(
    private val playersRepository: PlayersRepository
){
    operator fun invoke(gameId: String) = playersRepository.getAllPlayers(gameId)
}