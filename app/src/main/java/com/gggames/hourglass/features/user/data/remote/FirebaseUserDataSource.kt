package com.gggames.hourglass.features.user.data.remote

import com.gggames.hourglass.features.common.getUsersCollectionPath
import com.gggames.hourglass.features.user.data.UserDataSource
import com.gggames.hourglass.features.user.data.UserDataSource.UserResponse
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.remote.PlayerRaw
import com.gggames.hourglass.model.remote.toRaw
import com.gggames.hourglass.model.remote.toUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named
import timber.log.Timber

class FirebaseUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named("baseFirebaseCollection")
    private val baseCollection: String
) : UserDataSource {

    override fun getUser(userId: String): Observable<UserResponse> {
        val users = getUsersCollectionRef()
        return Observable.create { emitter ->
            users.document(userId).addSnapshotListener { value, e ->
                if (e == null) {
                    if (value?.exists() == true) {
                        val userRaw = value.toObject(PlayerRaw::class.java)
                        userRaw?.let {
                            emitter.onNext(UserResponse.Exists(userRaw.toUi()))
                        }
                    } else {
                        emitter.onNext(UserResponse.NotExists)
                    } ?: emitter.onError(IllegalStateException("Error getUser, userId: $userId"))
                } else {
                    Timber.e(e, "getUser, error")
                    emitter.onError(e)
                }
            }
        }
    }

    override fun setUser(user: Player): Completable {
        val userRaw = user.toRaw()
        return Completable.create { emitter ->
            getUsersCollectionRef()
                .document(userRaw.id).set(userRaw, SetOptions.merge()).addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to set user")
                    emitter.onError(error)
                }
        }
    }

    private fun getUsersCollectionRef() =
        firestore.collection(getUsersCollectionPath(baseCollection))
}
