package com.gggames.celebs.features.user.data

import com.gggames.celebs.model.User
import io.reactivex.Completable
import io.reactivex.Observable

interface UserDataSource {
    fun getUser(userId: String): Observable<UserResponse>
    fun addUser(user: User): Completable

    sealed class UserResponse {
        data class Data(val user: User): UserResponse()
        object NotExist: UserResponse()
    }
}