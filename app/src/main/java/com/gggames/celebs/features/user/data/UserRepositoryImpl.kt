package com.gggames.celebs.features.user.data

import com.gggames.celebs.model.User
import io.reactivex.Observable
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseUserDataSource: UserDataSource
) : UserRepository {

    override fun get(userId: String): Observable<User>  =
        firebaseUserDataSource.getUser(userId)
}