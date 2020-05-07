package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.players.data.PlayersRepository
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider

class ObservePlayers(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
){
    operator fun invoke(gameId: String) = playersRepository.getAllPlayers(gameId)
        .compose(schedulerProvider.applyDefault())
}