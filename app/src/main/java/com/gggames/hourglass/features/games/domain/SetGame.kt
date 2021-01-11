package com.gggames.hourglass.features.games.domain

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.setTimestamp
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.SetGameResult.Done
import io.reactivex.Observable
import io.reactivex.Observable.just
import timber.log.Timber
import javax.inject.Inject

class SetGame @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(game: Game, label: String? = null): Observable<out SetGameResult> =
        gamesRepository.setGame(game.setTimestamp(game.timestamp + 1))
            .andThen(just<SetGameResult>(Done(label)))
            .doOnSubscribe { label?.let { Timber.d("INTERNAL::$it") } }



    operator fun invoke(gameOperator: (game: Game) -> Game, label: String? = null): Observable<out SetGameResult> =
        gamesRepository.getCurrentGame().toObservable()
            .switchMap { currentGame ->
                gamesRepository.setGame(gameOperator(currentGame.setTimestamp(currentGame.timestamp + 1)))
                    .andThen(just<SetGameResult>(Done(label)))
                    .doOnSubscribe { label?.let { Timber.d("INTERNAL::$it") } }
            }
}
