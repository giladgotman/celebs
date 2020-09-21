package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.players.data.PlayersRepository
import javax.inject.Inject

class ObservePlayers @Inject constructor(
    private val playersRepository: PlayersRepository
) {
    operator fun invoke(gameId: String) = playersRepository.getAllPlayers(gameId)
}
