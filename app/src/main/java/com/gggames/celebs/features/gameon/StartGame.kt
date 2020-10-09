package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartGameResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartGameResult.Done
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartGameResult.InProgress
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class StartGame @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(player: Player, game: Game): Observable<out StartGameResult> {
        return setGame(
            game
                .setGameState(GameState.Started)
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running)
                .resetCardsFoundInTurn()
                .setTurnPlayer(player)
                .setTurnLastCards(emptyList())
        )
            .andThen(just<StartGameResult>(Done))
            .startWith(InProgress)
    }
}

