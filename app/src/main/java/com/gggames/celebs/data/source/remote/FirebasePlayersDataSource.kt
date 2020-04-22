package com.gggames.celebs.data.source.remote

import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.players.PlayersDataSource
import com.gggames.celebs.data.source.remote.model.PlayerRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber


class FirebasePlayersDataSource(
    private val firestore: FirebaseFirestore
) : PlayersDataSource {
    private val baseGamesPath = "games"

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


    override fun addPlayer(gameId: String, player: Player): Completable {
        val playersCollectionsRef = getCollectionReference(gameId)
        Timber.w("addPlayer: $player, playersCollectionsRef: ${playersCollectionsRef.path}")
        val playersRaw = player.toRaw()
        return Completable.create { emitter->
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

    private fun getGameRef(gameId: String) = firestore.document("$baseGamesPath/$gameId/")

    private fun getCollectionReference(gameRef: DocumentReference) =
        firestore.collection("${gameRef.path}/players/")

    private fun getCollectionReference(gameId: String) =
        getCollectionReference(getGameRef(gameId))

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable {
        val playersCollectionsRef = getCollectionReference(gameId)
        Timber.w("chooseTeam: ${player.name}, team: $teamName ref: ${playersCollectionsRef.path}")
        return Completable.create { emitter->
            playersCollectionsRef.document(player.id).update("team", teamName)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Timber.i("team chosen : $teamName for player: ${player.id}")
                        emitter.onComplete()
                    } else {
                        Timber.e(it.exception, "error while trying to choose team")
                        emitter.onError(it.exception ?: UnknownError(it.toString()))
                    }
                }
        }
    }
}



