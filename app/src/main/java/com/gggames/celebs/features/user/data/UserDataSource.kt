package com.gggames.celebs.features.user.data

import com.gggames.celebs.model.User
import io.reactivex.Observable

interface UserDataSource {
    fun getUser(userId: String): Observable<User>
}