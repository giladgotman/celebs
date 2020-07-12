package com.gggames.celebs.features.user.data

import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

interface UserDataSource {
    fun getUser(userId: String): Observable<UserResponse>
    fun setUser(user: Player): Completable

    sealed class UserResponse {
        data class Exists(val user: Player): UserResponse()
        object NotExists: UserResponse()
    }
}