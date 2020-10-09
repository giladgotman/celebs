package com.gggames.celebs.features.games.domain

import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.model.Game
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.SetGameResult.InProgress
import io.reactivex.Observable
import io.reactivex.Observable.just
import javax.inject.Inject

class SetGame @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(game: Game, label: String): Observable<out SetGameResult> =
        gamesRepository.setGame(game)
            .andThen(just<SetGameResult>(Done(label)))
            .startWith(InProgress(label))
}
