package com.gggames.celebs.features.user.domain

import com.gggames.celebs.features.user.data.UserRepository
import javax.inject.Inject

class ObserveUser @Inject constructor(
    private val userRepository: UserRepository
){
    operator fun invoke(userId: String) = userRepository.get(userId)
}