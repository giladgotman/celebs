package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.ShowRoundInstructionsResult
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowRoundInstructions @Inject constructor() {
    operator fun invoke(): Observable<ShowRoundInstructionsResult> =
        Observable.timer(1000, TimeUnit.MILLISECONDS).switchMap {
            Observable.just<ShowRoundInstructionsResult>(
                ShowRoundInstructionsResult(true),
                ShowRoundInstructionsResult(false)
            )
        }
}