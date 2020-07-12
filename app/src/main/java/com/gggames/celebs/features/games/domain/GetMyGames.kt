package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.user.domain.GetMyUser
import com.gggames.celebs.model.GameState
import javax.inject.Inject

/*
Gets games that are included in my user
 */
class GetMyGames @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val getMyUser: GetMyUser
){
    operator fun invoke() = getMyUser()
        .switchMap { gamesRepository.getGames(it.games, listOf(GameState.Created, GameState.Started)) }

}