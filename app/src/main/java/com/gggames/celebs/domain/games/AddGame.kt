package com.gggames.celebs.domain.games

import com.gggames.celebs.data.games.GamesRepository
import com.gggames.celebs.data.model.Game
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class AddGame(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game): Completable =
        gamesRepository.addGame(game)
            .compose(schedulerProvider.applyCompletableDefault())
}