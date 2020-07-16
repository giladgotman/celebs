package com.gggames.celebs.features.user.domain

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.user.data.UserDataSource
import com.gggames.celebs.model.User
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
                setUser(createLoggedInUser(username))
                    .andThen(authenticator.signup(username))
                    .andThen(
                        Single.just(
                            SignupResponse.Success(
                                createLoggedInUser(username)
                            )
                        )
                    )
            } else {
                Single.just(SignupResponse.UserAlreadyExists)
            }
        }
    }
}

fun createLoggedInUser(username: String) = User.LoggedIn(
    username,
    username,
    emptyList()
)

sealed class SignupResponse {
    data class Success(val user: User.LoggedIn) : SignupResponse()
    object UserAlreadyExists : SignupResponse()
}
