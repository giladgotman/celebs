package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.*
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.ResumeTurnResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.ResumeTurnResult.Done
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.ResumeTurnResult.InProgress
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class ResumeTurn @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Observable<out ResumeTurnResult> {
        return setGame(
            game
                .setRoundState(RoundState.Started)
                .setTurnState(TurnState.Running)
        )
            .andThen(just<ResumeTurnResult>(Done))
            .startWith(InProgress)
    }
}

