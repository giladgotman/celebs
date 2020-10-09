package com.gggames.celebs.features.gameon

import com.gggames.celebs.core.Authenticator
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.TurnState
import com.gggames.celebs.model.setTurnState
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.BackPressedResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class HandleBackPressed @Inject constructor(
    private val authenticator: Authenticator,
    private val setGame: SetGame
) {
    operator fun invoke(game: Game): Observable<out BackPressedResult> =
        if (authenticator.isMyselfActivePlayerBlocking(game)) {
            setGame(game.setTurnState(TurnState.Paused))
                .filter { it is Done }
                .switchMap {
                    just(
                        BackPressedResult.ShowLeaveGameConfirmation(true),
                        BackPressedResult.ShowLeaveGameConfirmation(false)
                    )
                }
        } else {
            just(
                BackPressedResult.NavigateToGames(true),
                BackPressedResult.NavigateToGames(false)
            )
        }
}

