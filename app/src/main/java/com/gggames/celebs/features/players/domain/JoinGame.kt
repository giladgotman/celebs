package com.gggames.celebs.features.players.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.features.user.domain.AddGameToUser
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject

class JoinGame @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val gamesRepository: GamesRepository,
    private val addGameToUser: AddGameToUser,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game, user: Player): Completable =
        playersRepository.addPlayer(game.id, user)
            .andThen(gamesRepository.setGame(game, updateRemote = false))
            .andThen(addGameToUser(user, game))
            .compose(schedulerProvider.applyCompletableDefault())
}
