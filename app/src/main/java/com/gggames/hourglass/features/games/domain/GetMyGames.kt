package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.user.domain.GetMyUser
import com.gggames.hourglass.model.GameState
import javax.inject.Inject

/*
Gets games that are included in my user
 */
class GetMyGames @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val getMyUser: GetMyUser
) {
    operator fun invoke() = getMyUser()
        .switchMap { gamesRepository.getGames(it.games, listOf(GameState.Created, GameState.Started)) }
}
