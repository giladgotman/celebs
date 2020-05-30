package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject

class LeaveGame @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game, player: Player): Completable =
        playersRepository.removePlayer(game.id, player)
            .andThen(gamesRepository.setGame(null, updateRemote = false))
            .compose(schedulerProvider.applyCompletableDefault())
}