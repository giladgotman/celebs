package com.gggames.celebs.features.user.domain

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.user.data.UserDataSource
import com.gggames.celebs.features.user.data.UserRepository
import javax.inject.Inject

class GetMyUser @Inject constructor(
    private val userRepository: UserRepository,
    private val authenticator: Authenticator
){
    operator fun invoke() =
        userRepository.get(authenticator.me!!.id).map {
            if (it is UserDataSource.UserResponse.NotExists) {
                authenticator.logout()
            }
            it
        }.filter { it is UserDataSource.UserResponse.Exists }
            .cast(UserDataSource.UserResponse.Exists::class.java)
            .map { it.user }
}