package com.gggames.hourglass.features.user.domain

import com.gggames.hourglass.features.user.data.UserRepository
import com.gggames.hourglass.model.Player
import javax.inject.Inject

class SetUser @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(user: Player) = userRepository.set(user)
}
