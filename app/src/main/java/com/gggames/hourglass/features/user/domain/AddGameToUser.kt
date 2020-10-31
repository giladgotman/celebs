package com.gggames.hourglass.features.user.domain

import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.Player
import io.reactivex.Completable
import javax.inject.Inject

class AddGameToUser @Inject constructor(
    private val setUser: SetUser
) {
    operator fun invoke(user: Player, game: Game): Completable {
        val mutableList = user.games.toMutableSet()
        mutableList.add(game.id)
        return setUser(user.copy(games = mutableList.toList()))
    }
}
