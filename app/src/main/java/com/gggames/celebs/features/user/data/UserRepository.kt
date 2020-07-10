package com.gggames.celebs.features.user.data

import com.gggames.celebs.features.user.data.UserDataSource.UserResponse
import com.gggames.celebs.model.User
import io.reactivex.Completable
import io.reactivex.Observable

interface UserRepository {
    fun get(userId: String): Observable<UserResponse>

    fun add(user: User): Completable
}

