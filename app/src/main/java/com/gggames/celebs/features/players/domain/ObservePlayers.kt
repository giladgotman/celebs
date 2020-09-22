package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.presentation.gameon.GameScreenContract
import javax.inject.Inject

class ObservePlayers @Inject constructor(
    private val playersRepository: PlayersRepository
) {
    operator fun invoke(gameId: String) =
        playersRepository.getAllPlayers(gameId)
            .distinctUntilChanged()
            .map { GameScreenContract.Result.PlayersUpdate(it) }
}
