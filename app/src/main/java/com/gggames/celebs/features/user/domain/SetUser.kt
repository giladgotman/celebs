package com.gggames.celebs.features.user.domain

import com.gggames.celebs.features.user.data.UserRepository
import com.gggames.celebs.model.User
import javax.inject.Inject

class SetUser @Inject constructor(
    private val userRepository: UserRepository
){
    operator fun invoke(user: User) = userRepository.set(user)
}