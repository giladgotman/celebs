package com.gggames.hourglass.features.games.data.remote

import com.gggames.hourglass.features.common.getGamesCollectionPath
import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import com.gggames.hourglass.model.remote.GameRaw
import com.gggames.hourglass.model.remote.toRaw
import com.gggames.hourglass.model.remote.toUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestoreSettings
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


interface RemoteGamesDataSource {
    fun getGames(gameIds: List<String>, states: List<GameState> = emptyList()): Single<List<Game>>

    fun setGame(game: Game): Completable

    fun observeGame(gameId: String): Observable<GameResult>
}

class FirebaseGamesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named("baseFirebaseCollection")
    private val baseCollection: String
) : RemoteGamesDataSource {
    override fun getGames(gameIds: List<String>, states: List<GameState>): Single<List<Game>> {
        return Single.create { emitter ->
            if (gameIds.isEmpty()) {
                emitter.onSuccess(emptyList())
            } else {
                val games = mutableListOf<Game>()
                if (gameIds.size > 10) {
                    Timber.w("can't get more then 10 games. size is : ${gameIds.size}")
                }
                val settings = firestoreSettings {
                    isPersistenceEnabled = true
                }

                firestore.firestoreSettings = settings
                val query = firestore.collection(getGamesCollectionPath(baseCollection))
                    // firebase is limited to 10 elements in a where in clause
                    .whereIn("id", gameIds.takeLast(10))
                    .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                    )
                query.get()
                    .addOnSuccessListener { result ->
                        for (game in result) {
                            val gameEntity = game.toObject(GameRaw().javaClass)
                            if (filterGame(gameEntity, states)) {
                                games.add(gameEntity.toUi())
                            }
                        }
                        emitter.onSuccess(games)
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Error getting games.")
                        emitter.onError(exception)
                    }
            }
        }
    }

    // filter games that are not auto created by google and in the selected states list
    private fun filterGame(
        game: GameRaw,
        states: List<GameState>
    ): Boolean {
        if (game.name == "text") return false // google test games
        if (game.type == "Gift") {
            return true
        } else {
            if (states.isNotEmpty()) {
                return game.state in states.map { it.toRaw() }
            }
        }
        return true
    }

    override fun setGame(game: Game): Completable {
        val gameRaw = game.toRaw()
        return Completable.create { emitter ->
            firestore.collection(getGamesCollectionPath(baseCollection))
                .document(gameRaw.id).set(gameRaw, SetOptions.merge()).addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to set game")
                    emitter.onError(error)
                }
        }
    }

    override fun observeGame(gameId: String): Observable<GameResult> {
        return Observable.create { emitter ->
            firestore.collection(getGamesCollectionPath(baseCollection))
                .document(gameId).addSnapshotListener { value, e ->
                    if (e == null) {
                        val gameRaw = value?.toObject(GameRaw::class.java)
                        gameRaw?.let {
                            emitter.onNext(GameResult.Found(gameRaw.toUi()))
                        }
                            ?: emitter.onError(IllegalStateException("GameId : $gameId could not be found"))
                    } else {
                        Timber.e(e, "observeGame, error or not found, gameId: $gameId")
                        emitter.onNext(GameResult.NotFound)
                    }
                }
        }
    }
}
