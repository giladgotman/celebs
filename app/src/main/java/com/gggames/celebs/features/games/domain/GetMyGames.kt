package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.user.data.UserDataSource
import com.gggames.celebs.features.user.domain.GetMyUser
import javax.inject.Inject

/*
Gets games that are included in my user
 */
class GetMyGames @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val getMyUser: GetMyUser
){
    operator fun invoke() = getMyUser()
        .filter{ it is UserDataSource.UserResponse.Exists}
        .cast(UserDataSource.UserResponse.Exists::class.java)
        .map { it.user }
        .switchMap { gamesRepository.getGames(it.games, emptyList()) }

}