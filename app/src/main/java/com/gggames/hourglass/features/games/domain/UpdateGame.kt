package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.model.Game
import io.reactivex.Completable
import javax.inject.Inject

class UpdateGame @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(game: Game): Completable =
        gamesRepository.setGame(game)
}
