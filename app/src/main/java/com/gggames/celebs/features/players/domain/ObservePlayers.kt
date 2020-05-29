package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PlayersResult
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class ObservePlayers @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
){
    operator fun invoke(gameId: String) = playersRepository.getAllPlayers(gameId)
        .compose(schedulerProvider.applyDefault())
        .map { PlayersResult(it) }
}