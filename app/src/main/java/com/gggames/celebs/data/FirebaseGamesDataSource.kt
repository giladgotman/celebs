package com.gggames.celebs.data

import android.util.Log
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Group
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Single


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
                        val gameEntity = game.toGameEntity()
                        Log.d(TAG, "gameEntity ${game.toGameEntity()}")
                        gameEntity?.let {
                            games.add(it)
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
}


fun DocumentSnapshot.toGameEntity() =
    this.data?.let { data ->
        Game(
            this.id,
            data["name"] as String,
            (data["createdAt"] as Timestamp).seconds,
            (data["celebsCount"] as Long).toInt(),
            data["groups"] as ArrayList<Group>
        )
    }



