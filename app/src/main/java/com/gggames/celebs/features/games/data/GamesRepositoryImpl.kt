package com.gggames.celebs.features.games.data

import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val gamesDataSource: GamesDataSource
) : GamesRepository {

    override var currentGame: Game? = null

    val gameId: String
        get() = currentGame!!.id

    override fun getGames(statesQuery: List<GameState>): Observable<List<Game>> {
        return gamesDataSource.getGames(statesQuery).toObservable()
    }

    // TODO: 13.06.20 don't set the game as a side effect. do it in the chain.
    // when doing this we should fix the updateGame operations in GamePresenter.
    override fun setGame(game: Game?, updateRemote: Boolean): Completable {
        currentGame = game
        Timber.w("setGame, currentGame: $currentGame")
        return if (updateRemote && game != null) {
            gamesDataSource.setGame(game)
        } else {
            Completable.complete()
        }
    }

    override fun observeGame(gameId: String): Observable<Game> =
        gamesDataSource.observeGame(gameId).doOnNext { currentGame = it }
}