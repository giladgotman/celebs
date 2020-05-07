package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.games.data.remote.GAMES_PATH
import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.remote.CardRaw
import com.gggames.celebs.model.remote.toRaw
import com.gggames.celebs.model.remote.toUi
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber


class FirebaseCardsDataSource(
    private val gameId: String,
    private val firestore: FirebaseFirestore
) : CardsDataSource {
    private val gameRef: DocumentReference
        get() = firestore.document("$GAMES_PATH/$gameId/")

    private val cardsCollectionsRef: CollectionReference
        get() = firestore.collection("${gameRef.path}/cards/")

    override fun getAllCards(): Observable<List<Card>> {
        Timber.d("fetching all cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        return Observable.create { emitter ->
            cardsCollectionsRef.addSnapshotListener { value, e ->
                if (e != null) {
                    Timber.e(e, "getAllCards, error")
                    emitter.onError(e)
                } else {
                    val cards =
                        value?.documents?.map { it.toObject(CardRaw::class.java)?.copy(id = it.id) } ?: emptyList()
                    emitter.onNext(cards.mapNotNull { it?.toUi() })
                    Timber.w("getAllCards update")
                }
            }
        }
    }

    override fun addCards(cards: List<Card>): Completable {
        Timber.w("addCards: $cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        val cardsRaw = cards.map { it.toRaw() }
        return Completable.create { emitter->
            firestore.runTransaction {
//                gameRef.update("state.myCards", cardsRaw)
                cardsRaw.forEach {
                    cardsCollectionsRef.add(it)
                }
            }.addOnSuccessListener {
                Timber.i("cards added to path: ${cardsCollectionsRef.path}")
                emitter.onComplete()
            }.addOnFailureListener { error ->
                Timber.e(error, "error while trying to add cards to path: ${cardsCollectionsRef.path}")
                emitter.onError(error)
            }
        }
    }

    override fun update(card: Card): Completable {
        Timber.w("update: $card, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        val cardRaw = card.toRaw()
        if (cardRaw.id == null) {
            return Completable.error(java.lang.IllegalArgumentException("CardRaw.id can't be null"))
        }
        return Completable.create { emitter->
                cardsCollectionsRef.document(cardRaw.id).set(cardRaw)
            .addOnSuccessListener {
                Timber.i("card updated in path: ${cardsCollectionsRef.path}")
                emitter.onComplete()
            }.addOnFailureListener { error ->
                Timber.e(error, "error while trying to update card in path: ${cardsCollectionsRef.path}")
                emitter.onError(error)
            }
        }
    }

    override fun updateCards(cards: List<Card>): Completable {
        Timber.w("updateCards: $cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        val cardsRaw = cards.map { it.toRaw() }
        return Completable.create { emitter ->
            firestore.runTransaction {
                cardsRaw.forEach {
                    if (it.id == null) {
                        Completable.error(java.lang.IllegalArgumentException("CardRaw.id can't be null"))
                    } else {
                        cardsCollectionsRef.document(it.id).set(it)
                    }
                }
            }.addOnSuccessListener {
                Timber.i("cards updated to path: ${cardsCollectionsRef.path}")
                emitter.onComplete()
            }.addOnFailureListener { error ->
                Timber.e(
                    error,
                    "error while trying to update cards to path: ${cardsCollectionsRef.path}"
                )
                emitter.onError(error)
            }
        }
    }
}



