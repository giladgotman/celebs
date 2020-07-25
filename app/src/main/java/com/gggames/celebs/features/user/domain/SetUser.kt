package com.gggames.celebs.features.user.domain

import com.gggames.celebs.features.user.data.UserRepository
import com.gggames.celebs.model.Player
import javax.inject.Inject

class SetUser @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(user: Player) = userRepository.set(user)
}
