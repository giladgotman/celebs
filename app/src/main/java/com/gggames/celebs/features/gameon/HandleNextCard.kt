package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.HandleNextCardResult
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class HandleNextCard @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val pickNextCard: PickNextCard,
    private val setGame: SetGame
) {
    operator fun invoke(cardDeck: List<Card>, game: Game, time: Long?): Observable<out HandleNextCardResult> =
        pickNextCard(cardDeck, game.type, game.round, time).switchMap { pickNextCardResult ->
            if (pickNextCardResult is PickNextCardResult.Found) {
                cardsRepository.updateCard(pickNextCardResult.card)
                    .andThen(just(HandleNextCardResult.NewCard(pickNextCardResult.card, time)))
            } else {
                val isLastRound: Boolean = (game.gameInfo.round.roundNumber == 3)
                if (isLastRound) {
                    setGame(game.setGameState(GameState.Finished))
                        .andThen(just(HandleNextCardResult.GameOver))
                } else {
                    val updatedGame = time?.let {
                        game.setTurnTime(it)
                    } ?: game
                        .setTurnState(TurnState.Paused)
                        .setRoundState(RoundState.Ended)
                    setGame(updatedGame).andThen(just(HandleNextCardResult.RoundOver(game.round, game.round, time)))
                }
                //todo check if need to check if round already in Ended state and then not doing anything
            }
        }
}


