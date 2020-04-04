package com.gggames.celebs.data

import android.util.Log
import com.gggames.celebs.data.model.Game
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseGamesDataSource(
    private val firestore: FirebaseFirestore
) : GamesDataSource {
    private val TAG = "gilad"

    override fun getGames(): List<Game> {
        Log.d(TAG, "fetching games");
        val games = mutableListOf<Game>()
        firestore.collection("games").get()
            .addOnSuccessListener { result ->
                for (game in result) {
                    Log.d(TAG, "${game.id} => ${game.data}")
                    val gameEntity = game.toObject(Game::class.java)
                    Log.d(TAG, "gameEntity ${gameEntity}")
                    games.add(gameEntity)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        Log.d(TAG, "games: $games");

        return games
    }
}
