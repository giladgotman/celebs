package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.TurnState
import com.gggames.celebs.model.setTurnState
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.PauseTurnResult
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class PauseTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Observable<out PauseTurnResult> {
        return setGame(
            game.setTurnState(TurnState.Paused)
        )
            .andThen(just<PauseTurnResult>(PauseTurnResult.Done))
            .startWith(PauseTurnResult.InProgress)
    }
}

