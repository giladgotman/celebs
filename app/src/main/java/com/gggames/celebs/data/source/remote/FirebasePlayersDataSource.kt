package com.gggames.celebs.data.source.remote

import com.gggames.celebs.data.PlayersDataSource
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.source.remote.model.PlayerRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber


class FirebasePlayersDataSource(
    private val firestore: FirebaseFirestore
) : PlayersDataSource {
    private val baseGamesPath = "games"

    override fun getAllPlayers(gameId: String): Single<List<Player>> {
        val playersCollectionsRef = getCollectionReference(gameId)
        Timber.d("fetching all players, playersCollectionsRef: ${playersCollectionsRef.path}")
        return Single.create { emitter ->
            playersCollectionsRef.get()
                .addOnSuccessListener { result ->
                    val players = result.documents.map { it.toObject(PlayerRaw::class.java) }
                    players.let {
                        emitter.onSuccess(players.mapNotNull { it?.toUi() })
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e(
                        exception,
                        "Error fetching players for path: ${playersCollectionsRef.path}"
                    )
                    emitter.onError(exception)
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



