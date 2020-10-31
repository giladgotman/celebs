package com.gggames.hourglass.features.user.data

import com.gggames.hourglass.features.user.data.UserDataSource.UserResponse
import com.gggames.hourglass.model.Player
import io.reactivex.Completable
import io.reactivex.Observable

interface UserRepository {
    fun get(userId: String): Observable<UserResponse>

    fun set(user: Player): Completable
}
