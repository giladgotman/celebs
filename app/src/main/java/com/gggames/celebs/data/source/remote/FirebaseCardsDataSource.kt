package com.gggames.celebs.data.source.remote

import com.gggames.celebs.data.CardsDataSource
import com.gggames.celebs.data.model.Card
import com.gggames.celebs.data.source.remote.model.CardRaw
import com.gggames.celebs.data.source.remote.model.GameRaw
import com.gggames.celebs.data.source.remote.model.toRaw
import com.gggames.celebs.data.source.remote.model.toUi
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber


class FirebaseCardsDataSource(
    private val gameId: String,
    private val firestore: FirebaseFirestore
) : CardsDataSource {
    private val baseGamesPath = "games"
    private val gameRef: DocumentReference
        get() = firestore.document("$baseGamesPath/$gameId/")

    private val cardsCollectionsRef: CollectionReference
        get() = firestore.collection("${gameRef.path}/cards/")

    override fun getMyCards(): Single<List<Card>> {
        Timber.d("fetching my cards, gameRef: ${gameRef.path}")
        return Single.create { emitter ->
            gameRef.get()
                .addOnSuccessListener { result ->
                    val cards = result.toObject(GameRaw().javaClass)?.state?.myCards
                    cards?.let {
                        emitter.onSuccess(cards.map { it.toUi() })
                    } ?: emitter.onError(IllegalArgumentException("cards are null for path: ${gameRef.path}"))
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception,"Error fetching cards for path: ${gameRef.path}")
                    emitter.onError(exception)
                }
        }
    }

    override fun getAllCards(): Single<List<Card>> {
        Timber.d("fetching all cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        return Single.create { emitter ->
            cardsCollectionsRef.get()
                .addOnSuccessListener { result ->
                    val cards = result.documents.map { it.toObject(CardRaw::class.java) }
                    cards.let {
                        emitter.onSuccess(cards.mapNotNull { it?.toUi() })
                    } ?: emitter.onError(IllegalArgumentException("cards are null for path: $gameRef"))
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception,"Error fetching cards for path: ${gameRef.path}")
                    emitter.onError(exception)
                }
        }
    }


    override fun addCards(cards: List<Card>): Completable {
        Timber.w("addCards: $cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        val cardsRaw = cards.map { it.toRaw() }
        return Completable.fromCallable {
            firestore.runTransaction {
                gameRef.update("state.myCards", cardsRaw)
                cardsRaw.forEach {
                    cardsCollectionsRef.add(it)
                }
            }.addOnSuccessListener {
                Timber.i("cards added to path: ${cardsCollectionsRef.path}")
                Completable.complete()
            }.addOnFailureListener { error ->
                Timber.e(error, "error while trying to add cards to path: ${cardsCollectionsRef.path}")
                Completable.error(error)
            }
        }
    }
}



