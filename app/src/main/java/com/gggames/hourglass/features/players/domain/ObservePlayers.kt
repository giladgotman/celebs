package com.gggames.hourglass.features.players.domain

import com.gggames.hourglass.features.players.data.PlayersRepository
import com.gggames.hourglass.presentation.gameon.GameScreenContract
import javax.inject.Inject

class ObservePlayers @Inject constructor(
    private val playersRepository: PlayersRepository
) {
    operator fun invoke(gameId: String) =
        playersRepository.getAllPlayers(gameId)
            .distinctUntilChanged()
            .map { GameScreenContract.Result.PlayersUpdate(it) }
}
