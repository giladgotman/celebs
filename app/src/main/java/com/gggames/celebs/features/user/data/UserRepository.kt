package com.gggames.celebs.features.user.data

import com.gggames.celebs.model.User
import io.reactivex.Observable

interface UserRepository {
    fun get(userId: String): Observable<User>
}

