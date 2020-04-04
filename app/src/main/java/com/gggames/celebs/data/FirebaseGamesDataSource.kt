package com.gggames.celebs.data

import android.util.Log
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Group
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseGamesDataSource(
    private val firestore: FirebaseFirestore
) : GamesDataSource {
    private val TAG = "gilad"

    override fun getGames(): List<Game> {
        Log.d(TAG, "fetching games")
        val games = mutableListOf<Game>()
        firestore.collection("games").get()
            .addOnSuccessListener { result ->
                for (game in result) {
                    val gameEntity = game.toGameEntity()
                    Log.d(TAG, "gameEntity ${game.toGameEntity()}")
                    gameEntity?.let {
                        games.add(it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        Log.d(TAG, "games: $games")
        return games
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



