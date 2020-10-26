package com.gggames.hourglass.features.players.data.remote

import com.gggames.hourglass.features.common.getGameCollectionPath
import com.gggames.hourglass.features.players.data.PlayersDataSource
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.model.remote.PlayerRaw
import com.gggames.hourglass.model.remote.toRaw
import com.gggames.hourglass.model.remote.toUi
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named
import timber.log.Timber

class FirebasePlayersDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named("baseFirebaseCollection")
    private val baseCollection: String
) : PlayersDataSource {

    override fun getAllPlayers(gameId: String): Observable<List<Player>> {
        val playersCollectionsRef = getCollectionReference(gameId)
        Timber.d("fetching all players, playersCollectionsRef: ${playersCollectionsRef.path}")
        return Observable.create { emitter ->
            playersCollectionsRef.addSnapshotListener { value, e ->
                if (e != null) {
                    Timber.e(e, "getAllPlayers, error")
                    emitter.onError(e)
                } else {
                    val players = mutableListOf<Player>()
                    value?.let {
                        for (player in it) {
                            val player = player.toObject(PlayerRaw().javaClass)
                            players.add(player.toUi())
                        }
                    }
                    emitter.onNext(players)
                    Timber.w("getAllPlayers update")
                }
            }
        }
    }

    override fun getPlayer(gameId: String, userId: String): Observable<Player> {
        return Observable.create { emitter ->
            getCollectionReference(gameId).document(userId)
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Timber.e(e, "getPlayer, error")
                        emitter.onError(e)
                    } else {
                        val player = value?.toObject(PlayerRaw().javaClass)
                        player?.let {
                            emitter.onNext(it.toUi())
                        }
                    }
                }
        }
    }

    override fun addPlayer(gameId: String, player: Player): Completable {
        val playersCollectionsRef = getCollectionReference(gameId)
        val playersRaw = player.toRaw()
        return Completable.create { emitter ->
            playersCollectionsRef.document(playersRaw.id).set(playersRaw)
                .addOnSuccessListener {
                    Timber.i("player added to path: ${playersCollectionsRef.path}")
                    emitter.onComplete()
                }.addOnFailureListener { error ->
                    Timber.e(
                        error,
                        "error while trying to add player to path: ${playersCollectionsRef.path}"
                    )
                    emitter.onError(error)
                }
        }
    }

    private fun getGameRef(gameId: String) = firestore.document(getGameCollectionPath(baseCollection, gameId))

    private fun getCollectionReference(gameRef: DocumentReference) =
        firestore.collection("${gameRef.path}/players/")

    private fun getCollectionReference(gameId: String) =
        getCollectionReference(getGameRef(gameId))

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable {
        val playersCollectionsRef = getCollectionReference(gameId)
        return Completable.create { emitter ->
            playersCollectionsRef.document(player.id).update("team", teamName)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        Timber.e(it.exception, "error while trying to choose team")
                        emitter.onError(it.exception ?: UnknownError(it.toString()))
                    }
                }
        }
    }

    override fun removePlayer(gameId: String, player: Player): Completable {
        val playersCollectionsRef = getCollectionReference(gameId)
        val playersRaw = player.toRaw()
        return Completable.create { emitter ->
            playersCollectionsRef.document(playersRaw.id).delete()
                .addOnSuccessListener {
                    Timber.i("player removed. path: ${playersCollectionsRef.path}")
                    emitter.onComplete()
                }.addOnFailureListener { error ->
                    Timber.e(
                        error,
                        "error while trying to remove player. path: ${playersCollectionsRef.path}"
                    )
                    emitter.onError(error)
                }
        }
    }
}
