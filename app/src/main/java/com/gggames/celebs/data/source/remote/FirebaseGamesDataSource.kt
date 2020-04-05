package com.gggames.celebs.data.source.remote

import android.util.Log
import com.gggames.celebs.data.GamesDataSource
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.source.remote.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
                        val gameEntity = game.toGameRaw()
                        Log.d(TAG, "gameEntity ${game.toGameRaw()}")
                        gameEntity?.let {
                            games.add(it.toUi())
                        }
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
        return Completable.complete()
    }
}


fun DocumentSnapshot.toGameRaw() =
    this.data?.let { data ->
        GameRaw(
            this.id,
            data["name"] as String,
            data["createdAt"] as Timestamp,
            data["celebsCount"] as Long,
            data["groups"] as ArrayList<GroupRaw>,
            data["rounds"] as ArrayList<RoundRaw>,
            parseGameState(data["state"])
        )
    }

fun parseGameState(any: Any?): GameStateRaw {
    Timber.w("any: $any")
    return GameStateRaw(state = "created", myCards = listOf(CardRaw("Putin")), otherCardsCount = mapOf(PlayerRaw("gilad") to 5))
}



