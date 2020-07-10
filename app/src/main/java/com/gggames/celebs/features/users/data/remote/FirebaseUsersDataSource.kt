package com.gggames.celebs.features.users.data.remote

import com.gggames.celebs.features.common.getUsersCollectionPath
import com.gggames.celebs.features.users.data.UsersDataSource
import com.gggames.celebs.model.User
import com.gggames.celebs.model.remote.UserRaw
import com.gggames.celebs.model.remote.toUi
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


class FirebaseUsersDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named ("baseFirebaseCollection")
    private val baseCollection: String
) : UsersDataSource {

    override fun getUser(userId: String): Observable<User> {
        val users = getUsersCollectionRef()
        return Observable.create { emitter ->
            users.document(userId).addSnapshotListener { value, e ->
                if (e == null) {
                    val userRaw = value?.toObject(UserRaw::class.java)
                    userRaw?.let {
                        emitter.onNext(userRaw.toUi())
                    }
                        ?: emitter.onError(IllegalStateException("getUser : $userId could not be found"))
                    Timber.w("getUser update")
                } else {
                    Timber.e(e, "getUser, error")
                    emitter.onError(e)
                }
            }
        }
    }

    private fun getUsersCollectionRef() = firestore.collection(getUsersCollectionPath(baseCollection))
}



