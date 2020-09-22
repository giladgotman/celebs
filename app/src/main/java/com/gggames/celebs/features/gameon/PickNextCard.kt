package com.gggames.celebs.features.gameon

import com.gggames.celebs.model.Card
import com.gggames.celebs.model.GameType
import com.gggames.celebs.model.Round
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.Found
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PickNextCardResult.NoCardsLeft
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class PickNextCard @Inject constructor(
) {
    operator fun invoke(cardDeck: List<Card>, gameType: GameType, round: Round, time:Long?): Observable<PickNextCardResult> {
        return Observable.fromCallable {
            val unUsedCards = cardDeck.filter { !it.used }
            val card = if (gameType == GameType.Gift && round.roundNumber == 1) {
                if (unUsedCards.isNotEmpty()) unUsedCards.minBy { it.index }!!
                    .copy(used = true) else null
            } else {
                if (unUsedCards.isNotEmpty()) unUsedCards.random().copy(used = true) else null
            }
            Timber.d("pickNextCard, card: $card")
            card?.let { Found(it) } ?: NoCardsLeft(round, time)
        }
    }
}

