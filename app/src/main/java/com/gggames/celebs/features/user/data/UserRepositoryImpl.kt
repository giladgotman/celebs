package com.gggames.celebs.features.user.data

import com.gggames.celebs.features.user.data.UserDataSource.UserResponse
import com.gggames.celebs.model.User
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseUserDataSource: UserDataSource
) : UserRepository {

    override fun get(userId: String): Observable<UserResponse>  =
        firebaseUserDataSource.getUser(userId)

    // if the user exists it will update the existing one, if not it will create a new one
    override fun set(user: User): Completable =
        firebaseUserDataSource.setUser(user)
}