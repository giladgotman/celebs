package com.gggames.celebs.features.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.model.Game
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.BackPressedResult
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class HandleBackPressed @Inject constructor(
    private val authenticator: Authenticator
) {
    operator fun invoke(game: Game): Observable<BackPressedResult> =
        if (authenticator.isMyselfActivePlayerBlocking(game)) {
            just(
                BackPressedResult.ShowLeaveGameConfirmation(true),
                BackPressedResult.ShowLeaveGameConfirmation(false)
            )
        } else {
            just(BackPressedResult.NavigateToGames)
        }
}

