package com.gggames.celebs.presentation.creategame

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.GetGames
import com.gggames.celebs.features.games.domain.ObserveGame
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.model.Game
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val getGames: GetGames,
    private val observeGame: ObserveGame,
    private val gameFlow: GameFlow,
    private val joinGame: JoinGame
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

        gameIdFromDeepLink?.let {
            observeGame(it).take(1).subscribe({
                joinGameAndGoToAddCards(it)
            }, {
                Timber.e(it, "Error trying to joing game: $it")
            }).let { disposables.add(it) }
        }?: fetchGames()
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
                view.showError()
            }).let { disposables.add(it) }
    }

    private fun fetchGames() {
        view.showLoading(true)
        getGames()
            .subscribe(
                { games ->
                    view.showLoading(false)
                    view.show(games)
                },
                {
                    Timber.e(it, "error fetching games")
                    view.showLoading(false)
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
        fun showError()
        fun showNeedLoginInfo()
        fun showLoading(show: Boolean)
        fun finishScreen()
        fun navigateToAddCards()
    }
}