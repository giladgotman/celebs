package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.Game
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject

class AddGame @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game): Completable =
        gamesRepository.addGame(game)
            .compose(schedulerProvider.applyCompletableDefault())
}