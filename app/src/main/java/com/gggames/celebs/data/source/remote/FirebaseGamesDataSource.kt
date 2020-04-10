package com.gggames.celebs.data.source.remote

import android.util.Log
import com.gggames.celebs.data.GamesDataSource
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.source.remote.model.GameRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber


class FirebaseGamesDataSource(
    private val firestore: FirebaseFirestore
) : GamesDataSource {
    private val TAG = "gilad"

    override fun getGames(): Single<List<Game>> {
        Log.d(TAG, "fetching games")
        val games = mutableListOf<Game>()
        return Single.create<List<Game>> { emitter ->
            firestore.collection("games").get()
                .addOnSuccessListener { result ->
                    for (game in result) {
                        val gameEntity = game.toObject(GameRaw().javaClass)
//                        val gameEntity = game.toGameRaw()
                            games.add(gameEntity.toUi())
                    }
                    emitter.onSuccess(games)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                    emitter.onError(exception)
                }


        }
    }

    override fun addGame(game: Game): Completable {
        Timber.w("addGame: $game")
        val gameRaw = game.toRaw()
        Timber.w("gameRaw: $gameRaw")
        return Completable.fromCallable {
            firestore.collection("games")
                .document(gameRaw.id).set(gameRaw).addOnSuccessListener {
                    Timber.i("game added to firebase")
                    Completable.complete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to add game")
                }
        }
    }
}



