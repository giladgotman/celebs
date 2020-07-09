package com.gggames.celebs.features.games.data.remote

import com.gggames.celebs.features.common.getGamesCollectionPath
import com.gggames.celebs.features.games.data.GamesDataSource
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import com.gggames.celebs.model.remote.GameRaw
import com.gggames.celebs.model.remote.toRaw
import com.gggames.celebs.model.remote.toUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class FirebaseGamesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    @Named("baseFirebaseCollection")
    private val baseCollection: String
) : GamesDataSource {
    override fun getGames(statesQuery: List<GameState>): Single<List<Game>> {
        val games = mutableListOf<Game>()
        return Single.create { emitter ->
            val query = if (statesQuery.isNotEmpty()) {
                firestore.collection(getGamesCollectionPath(baseCollection))
                    .whereIn("state", statesQuery.map { it.toRaw() })
            } else {
                firestore.collection(getGamesCollectionPath(baseCollection)).whereIn(
                    "state",
                    GameState.values().map { it.toRaw() }
                )
            }.orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )

            query.get()
                .addOnSuccessListener { result ->
                    for (game in result) {
                        val gameEntity = game.toObject(GameRaw().javaClass)
                        if (!filterGame(gameEntity)) {
                            games.add(gameEntity.toUi())
                        }
                    }
                    emitter.onSuccess(games)
                }
                .addOnFailureListener { exception ->
                    Timber.w(exception, "Error getting documents.")
                    emitter.onError(exception)
                }
        }
    }

    // filter out games that are auto created by google
    private fun filterGame(gameEntity: GameRaw) = gameEntity.name == "text"

    override fun setGame(game: Game): Completable {
        Timber.w("setGame: $game")
        val gameRaw = game.toRaw()
        return Completable.create { emitter ->
            firestore.collection(getGamesCollectionPath(baseCollection))
                .document(gameRaw.id).set(gameRaw, SetOptions.merge()).addOnSuccessListener {
                    Timber.i("game set to firebase")
                    emitter.onComplete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to set game")
                    emitter.onError(error)
                }
        }
    }

    override fun observeGame(gameId: String): Observable<Game> {
        Timber.w("observeGame: $gameId")
        return Observable.create { emitter ->
            firestore.collection(getGamesCollectionPath(baseCollection))
                .document(gameId).addSnapshotListener { value, e ->
                    if (e == null) {
                        val gameRaw = value?.toObject(GameRaw::class.java)
                        gameRaw?.let {
                            emitter.onNext(gameRaw.toUi())
                        }
                            ?: emitter.onError(IllegalStateException("GameId : $gameId could not be found"))

                        Timber.w("getAllCards update")
                    } else {
                        Timber.e(e, "observeGame, error")
                        emitter.onError(e)
                    }
                }
        }
    }
}





