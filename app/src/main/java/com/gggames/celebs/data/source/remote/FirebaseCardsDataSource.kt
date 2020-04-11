package com.gggames.celebs.data.source.remote

import com.gggames.celebs.data.CardsDataSource
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.source.remote.model.GameRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber


class FirebaseCardsDataSource(
    private val gameId: String,
    private val firestore: FirebaseFirestore
) : CardsDataSource {
    private val path = "games\\$gameId"

    override fun getAllCards(): Single<List<Card>> {
        Timber.d("fetching cards")
        return Single.create<List<Card>> { emitter ->
            firestore.document(path).get()
                .addOnSuccessListener { result ->
                    val cards = result.toObject(GameRaw().javaClass)?.state?.myCards
                    cards?.let {
                        emitter.onSuccess(cards.map { it.toUi() })
                    } ?: emitter.onError(IllegalArgumentException("cards are null for path: $path"))
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception,"Error fetching cards for path: $path")
                    emitter.onError(exception)
                }
        }
    }


    override fun addCards(cards: List<Card>): Completable {
        Timber.w("addCards: $cards")
        val cardsRaw = cards.map { it.toRaw() }
        Timber.w("cardsRaw: $cardsRaw")
        val addPath = "games\\$gameId"
        return Completable.fromCallable {
            firestore.document(path).update("state.myCards", FieldValue.arrayUnion(cardsRaw))
                .addOnSuccessListener {
                    Timber.i("cards added to path: $addPath")
                    Completable.complete()
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "error while trying to add cards to path: $addPath")
                    Completable.error(error)
                }
        }
    }
}



