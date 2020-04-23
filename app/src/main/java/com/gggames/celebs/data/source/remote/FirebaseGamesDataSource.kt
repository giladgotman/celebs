package com.gggames.celebs.data.source.remote

import android.util.Log
import com.gggames.celebs.data.games.GamesDataSource
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
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
        return Single.create { emitter ->
            firestore.collection(GAMES_PATH).get()
                .addOnSuccessListener { result ->
                    for (game in result) {
                        val gameEntity = game.toObject(GameRaw().javaClass)
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
        return Completable.create { emitter->
            Timber.w("updating firestore...")
            firestore.collection(GAMES_PATH)
                .document(gameRaw.id).set(gameRaw).addOnSuccessListener {
                    Timber.i("game added to firebase")
                    emitter.onComplete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to add game")
                    emitter.onError(error)
                }
        }
    }

    override fun chooseTeam(gameId: String, player: Player, teamName: String): Completable {
        Timber.w("chooseTeam: ${player.name}, team: $teamName")
        return Completable.create { emitter->
//            firestore.collection("games")
//                .document(gameId).set(gameRaw).addOnSuccessListener {
//                    Timber.i("game added to firebase")
//                    Completable.complete()
//                }
//                .addOnFailureListener { error ->
//                    Timber.e(error, "error while trying to add game")
//                    Completable.error(error)
//                }
        }
    }
}

val GAMES_PATH = "games2"



