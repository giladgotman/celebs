package com.gggames.celebs.features.users.data

import com.gggames.celebs.model.User
import io.reactivex.Observable

interface UsersDataSource {
    fun getUser(userId: String): Observable<User>
}