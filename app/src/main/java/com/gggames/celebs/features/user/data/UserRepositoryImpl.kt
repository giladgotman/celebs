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

    override fun add(user: User): Completable =
        get(user.id).flatMapCompletable {
            if (it is UserResponse.NotExist) {
                firebaseUserDataSource.addUser(user)
            } else {
                Completable.error(IllegalStateException("alreadyExists"))
            }
        }

}