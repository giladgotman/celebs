package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.RoundState
import com.gggames.celebs.model.setRoundState
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartRoundResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartRoundResult.Done
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.StartRoundResult.InProgress
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class StartRound @Inject constructor(
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Observable<out StartRoundResult> {
        return setGame(
            game
                .setRoundState(RoundState.Started)
        )
            .andThen(just<StartRoundResult>(Done))
            .startWith(InProgress)
    }
}

