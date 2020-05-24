package com.gggames.celebs.presentation.creategame

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.domain.GetGames
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.features.players.domain.LeaveGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameState
import com.gggames.celebs.presentation.creategame.GamesPresenter.Result.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val getGames: GetGames,
    private val observeGame: ObserveGame,
    private val gameFlow: GameFlow,
    private val joinGame: JoinGame,
    private val leaveGame: LeaveGame
) {
    lateinit var view: View
    private val disposables = CompositeDisposable()

    fun bind(view: View, gameIdFromDeepLink: String?) {
        this.view = view
        val playerName = gameFlow.me?.name
        if (playerName == null) {
            gameIdFromDeepLink?.let {
                view.showNeedLoginInfo()
            }
            logout()
            return
        }

        leaveGameIfNeeded()
            .andThen(isDeepLinkExists(gameIdFromDeepLink))
            .subscribe({ result ->
                when (result) {
                    is NoDeepLink -> fetchGames()
                    is GameFinished -> {
                        view.showJoinedGameIsFinished(result.gameName)
                        fetchGames()
                    }
                    is DeepLinkExists -> {
                        joinGameAndGoToAddCards(result.game)
                    }
                }
            }, {
                Timber.e(it, "error on bind")
                view.showGenericError()
            }).let { disposables.add(it) }
    }

    private fun leaveGameIfNeeded(): Completable {
        return gamesRepository.currentGame?.let {
            leaveGame(it, gameFlow.me!!)
        } ?: Completable.complete()
    }

    private fun isDeepLinkExists(gameIdFromDeepLink: String?): Observable<out Result> {
        return gameIdFromDeepLink?.let {
            observeGame(it).take(1)
                .map { game ->
                    if (game.state == GameState.Finished) {
                        GameFinished(game.name)
                    } else {
                        DeepLinkExists(game)
                    }
                }
        } ?: Observable.just(NoDeepLink)
    }

    private fun logout() {
        view.finishScreen()
        gameFlow.logout()
    }

    private fun joinGameAndGoToAddCards(game: Game) {
        joinGame(game, gameFlow.me!!)
            .subscribe({
                view.navigateToAddCards()
            }, {
                Timber.e(it, "error joinGame")
                view.showGenericError()
            }).let { disposables.add(it) }
    }

    private fun fetchGames() {
        getGames()
            .doOnSubscribe { view.showLoading(true) }
            .subscribe(
                { games ->
                    view.showLoading(false)
                    view.show(games)
                },
                {
                    Timber.e(it, "error fetching games")
                    view.showLoading(false)
                    view.showGenericError()
                }).let { disposables.add(it) }
    }

    fun unBind() {
        disposables.clear()
    }

    fun onRefresh() {
        fetchGames()
    }

    fun onGameClick(game: Game) {
        joinGameAndGoToAddCards(game)
    }

    interface View {
        fun show(games: List<Game>)
        fun showGenericError()
        fun showNeedLoginInfo()
        fun showLoading(show: Boolean)
        fun finishScreen()
        fun navigateToAddCards()
        fun showJoinedGameIsFinished(gameName: String)
    }

    sealed class Result {
        data class GameFinished(val gameName: String) : Result()
        object NoDeepLink : Result()
        data class DeepLinkExists(val game: Game) : Result()
    }
}