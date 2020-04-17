package com.gggames.celebs.data.source.remote

import com.gggames.celebs.data.PlayersDataSource
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.source.remote.model.PlayerRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber


class FirebasePlayersDataSource(
    private val firestore: FirebaseFirestore
) : PlayersDataSource {
    private val baseGamesPath = "games"

    override fun getAllPlayers(gameId: String): Single<List<Player>> {
        val gameRef = firestore.document("$baseGamesPath/$gameId/")
        val playersCollectionsRef = firestore.collection("${gameRef.path}/players/")

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
                    Timber.e(exception, "Error fetching players for path: ${gameRef.path}")
                    emitter.onError(exception)
                }
        }
    }

    override fun addPlayer(gameId: String, player: Player): Completable {
        val gameRef = firestore.document("$baseGamesPath/$gameId/")
        val playersCollectionsRef = firestore.collection("${gameRef.path}/players/")
        Timber.w("addPlayer: $player, playersCollectionsRef: ${playersCollectionsRef.path}")
        val playersRaw = player.toRaw()
        return Completable.fromCallable {
            playersCollectionsRef.add(playersRaw)
                .addOnSuccessListener {
                    Timber.i("player added to path: ${playersCollectionsRef.path}")
                    Completable.complete()
                }.addOnFailureListener { error ->
                    Timber.e(
                        error,
                        "error while trying to add player to path: ${playersCollectionsRef.path}"
                    )
                    Completable.error(error)
                }
        }
    }
}



