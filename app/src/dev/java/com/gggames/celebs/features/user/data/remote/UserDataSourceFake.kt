package com.gggames.celebs.features.user.data.remote

import com.gggames.celebs.features.user.data.UserDataSource
import com.gggames.celebs.model.Player
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

val fakePlayer = Player("fakeId", "fakeName")

class UserDataSourceFake @Inject constructor() : UserDataSource {
    private var users = mutableListOf(fakePlayer)
    private val userSubject = PublishSubject.create<Player>()

    override fun getUser(userId: String): Observable<UserDataSource.UserResponse> {
        val current = Observable.fromCallable {
            users.firstOrNull { it.id == userId || it.id == "fakeId" }
                ?.let { UserDataSource.UserResponse.Exists(it) }
                ?: UserDataSource.UserResponse.NotExists
        }
        return merge(current, userSubject.map { UserDataSource.UserResponse.Exists(it) })
    }

    override fun setUser(user: Player): Completable =
        Completable.fromCallable {
            users.indexOfFirst { it.id == user.id }.takeIf { it != -1 }?.let { index ->
                users.set(index, user)
            } ?: users.add(user)
            userSubject.onNext(user)
        }
}