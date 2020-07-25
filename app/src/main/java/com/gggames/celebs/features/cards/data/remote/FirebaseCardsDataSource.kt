package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.features.common.getGameCollectionPath
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
import javax.inject.Inject
import javax.inject.Named


class FirebaseCardsDataSource @Inject constructor(
    @Named("GameId")
    private val gameId: String,
    private val firestore: FirebaseFirestore,
    @Named("baseFirebaseCollection")
    private val baseCollection: String
) : CardsDataSource {
    private val gameRef: DocumentReference
        get() = firestore.document(getGameCollectionPath(baseCollection, gameId))

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
                        value?.documents?.map { it.toObject(CardRaw::class.java)?.copy(id = it.id) }
                            ?: emptyList()

//                    emitter.onNext(cards.mapNotNull { it?.toUi() })
                    // TODO: 24.07.20 FAKE ! remove me
                    val fakeCards = getFakeCards(cards.filterNotNull())
                    emitter.onNext(fakeCards.map { it.toUi() })
                    Timber.w("getAllCards update")
                }
            }
        }
    }

    private fun getFakeCards(cards: List<CardRaw>): List<CardRaw> =
        cards.map { it.copy(
            videoUrl1 = "https://drive.google.com/uc?export=download&id=194rl8msLR47b8No3-uuI-AmLre2wgoC9",
            videoUrl2 = "https://drive.google.com/uc?export=download&id=147xu8GaVe25o3LhJ6xNcElqeEEHD6_vW",
            videoUrl3 = "https://drive.google.com/uc?export=download&id=1CGIg6YgKin7m-QmHvyQ03omj6yEvWFRG",
            videoUrlFull = "https://drive.google.com/uc?export=download&id=1k-6jLFqi7YO_QgeCfA_ubU22_vLY-2AO"

        ) }



    override fun addCards(cards: List<Card>): Completable {
        Timber.w("addCards: $cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
        val cardsRaw = cards.map { it.toRaw() }
        return Completable.create { emitter ->
            firestore.runTransaction {
                cardsRaw.forEach {
                    cardsCollectionsRef.add(it)
                }
            }.addOnSuccessListener {
                Timber.i("cards added to path: ${cardsCollectionsRef.path}")
                emitter.onComplete()
            }.addOnFailureListener { error ->
                Timber.e(
                    error,
                    "error while trying to add cards to path: ${cardsCollectionsRef.path}"
                )
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
        return Completable.create { emitter ->
            cardsCollectionsRef.document(cardRaw.id).set(cardRaw)
                .addOnSuccessListener {
                    Timber.i("card updated in path: ${cardsCollectionsRef.path}")
                    emitter.onComplete()
                }.addOnFailureListener { error ->
                    Timber.e(
                        error,
                        "error while trying to update card in path: ${cardsCollectionsRef.path}"
                    )
                    emitter.onError(error)
                }
        }
    }

    override fun updateCards(cards: List<Card>): Completable =
        Completable.create { emitter ->
            Timber.w("updateCards: $cards, cardsCollectionsRef: ${cardsCollectionsRef.path}")
            val cardsRaw = cards.map { it.toRaw() }
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



