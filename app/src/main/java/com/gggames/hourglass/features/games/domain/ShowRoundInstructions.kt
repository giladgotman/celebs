package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.model.GameState
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.ShowRoundInstructionsResult
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowRoundInstructions @Inject constructor(val gamesRepository: GamesRepository) {
    operator fun invoke(): Observable<ShowRoundInstructionsResult> =
        gamesRepository.getCurrentGame()
            .filter { it.state == GameState.Created }
            .flatMapObservable {
                Observable.timer(1000, TimeUnit.MILLISECONDS).switchMap {
                    Observable.just<ShowRoundInstructionsResult>(
                        ShowRoundInstructionsResult(true),
                        ShowRoundInstructionsResult(false)
                    )
                }
            }
}