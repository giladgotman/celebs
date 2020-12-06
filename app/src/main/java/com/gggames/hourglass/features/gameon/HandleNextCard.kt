package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.model.*
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.HandleNextCardResult
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.HandleNextCardResult.NewCard
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class HandleNextCard @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val gamesRepository: GamesRepository,
    private val pickNextCard: PickNextCard,
    private val setGame: SetGame
) {
    operator fun invoke(cardDeck: List<Card>, time: Long?): Observable<out HandleNextCardResult> =
        gamesRepository.getCurrentGame().toObservable().switchMap { game ->
            pickNextCard(cardDeck, game.type, game.round, time).switchMap { pickNextCardResult ->
                if (pickNextCardResult is PickNextCardResult.Found) {
                    setGame(
                        game
                            .setCurrentCard(pickNextCardResult.card)
                            .setTurnTime(time ?: game.turn.time)
                    )
                    .filter { it is Done }
                        .switchMapCompletable { cardsRepository.updateCard(pickNextCardResult.card) }
                        .andThen(just(NewCard(pickNextCardResult.card, time)))
                } else {
                    val isLastRound: Boolean = (game.gameInfo.round.roundNumber == 3)
                    if (isLastRound) {
                        setGame(game.setGameState(GameState.Finished))
                            .filter { it is Done }
                            .switchMap { just(HandleNextCardResult.GameOver) }
                    } else {

                        val resetCards =
                            cardsRepository.setCards(cardDeck.map { it.copy(used = false) })

                        val endedRoundGame = game
                            .setRoundState(RoundState.Ended)
                            .setTurnState(TurnState.Paused)
                            .setTurnTime(time ?: game.turn.time)
                            .setCurrentCard(null)

                        val endRound = setGame(endedRoundGame)

                        val startNewRound = setGame(
                            endedRoundGame.setRoundState(RoundState.New)
                                .setRoundNumber(game.round.roundNumber + 1)
                        )

                        endRound
                            .filter { it is Done }
                            .switchMap { startNewRound }
                            .filter { it is Done }
                            .switchMapCompletable { resetCards }
                            .andThen(just(HandleNextCardResult.RoundOver(game.round, game.round, time)))
                    }
                    //todo check if need to check if round already in Ended state and then not doing anything
                }
            }.startWith(HandleNextCardResult.InProgress)
        }
}


