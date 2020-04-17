package com.gggames.celebs.domain

import com.gggames.celebs.data.PlayersRepository
import com.gggames.celebs.data.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Single

class GetMyPlayer(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(): Single<Player> =
        playersRepository.me()
            .compose(schedulerProvider.applySingleDefault())
}