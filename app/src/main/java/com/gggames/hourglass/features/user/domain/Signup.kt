package com.gggames.hourglass.features.user.domain

import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.features.user.data.UserDataSource
import com.gggames.hourglass.model.Player
import io.reactivex.Single
import javax.inject.Inject

class Signup @Inject constructor(
    private val observeUser: ObserveUser,
    private val setUser: SetUser,
    private val authenticator: Authenticator
) {
    operator fun invoke(username: String): Single<SignupResponse> {
        return observeUser(username).firstOrError().flatMap {
            if (it is UserDataSource.UserResponse.NotExists) {
                setUser(createPlayer(username))
                    .andThen(authenticator.signup(username))
                    .andThen(
                        Single.just(
                            SignupResponse.Success(
                                createPlayer(username)
                            )
                        )
                    )
            } else {
                Single.just(SignupResponse.UserAlreadyExists)
            }
        }
    }
}

private fun createPlayer(username: String) = Player(
    username,
    username,
    null,
    emptyList()
)

sealed class SignupResponse {
    data class Success(val user: Player) : SignupResponse()
    object UserAlreadyExists : SignupResponse()
}
