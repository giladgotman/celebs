package com.gggames.celebs.features.user.data.remote

import com.gggames.celebs.features.user.data.UserDataSource
import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class UserDataSourceFake @Inject constructor() : UserDataSource{
    override fun getUser(userId: String): Observable<UserDataSource.UserResponse> {
        TODO("Not yet implemented")
    }

    override fun setUser(user: Player): Completable {
        TODO("Not yet implemented")
    }
}