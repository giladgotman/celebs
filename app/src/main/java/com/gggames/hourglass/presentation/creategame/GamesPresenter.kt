package com.gggames.hourglass.presentation.creategame

import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.domain.GetMyGames
import com.gggames.hourglass.features.games.domain.ObserveGame
import com.gggames.hourglass.features.players.domain.JoinGame
import com.gggames.hourglass.features.players.domain.LeaveGame
import com.gggames.hourglass.features.user.domain.GetMyUser
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.GameState
import com.gggames.hourglass.presentation.creategame.GamesPresenter.Result.*
import com.gggames.hourglass.utils.prefs.PreferenceManager
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val getGames: GetMyGames,
    private val observeGame: ObserveGame,
    private val authenticator: Authenticator,
    private val getMyUser: GetMyUser,
    private val joinGame: JoinGame,
    private val leaveGame: LeaveGame,
    private val schedulerProvider: BaseSchedulerProvider,
    private val preferenceManager: PreferenceManager
) {
    lateinit var view: View
    private val disposables = CompositeDisposable()

    fun bind(view: View, gameIdFromDeepLink: String?) {
        this.view = view
        val playerName = authenticator.me?.name
        if (playerName == null) {
            gameIdFromDeepLink?.let {
                preferenceManager.saveGameInvitation(it)
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
                        view.showApproveJoinGame(result.game)
                    }
                }
            }, {
                Timber.e(it, "error on bind")
                view.showGenericError()
            }).let { disposables.add(it) }
    }

    private fun leaveGameIfNeeded(): Completable {
        return gamesRepository.getCurrentGameBlocking()?.let {
            leaveGame(it, authenticator.me!!)
        } ?: Completable.complete()
    }

    private fun isDeepLinkExists(gameIdFromDeepLink: String?): Observable<out Result> {
        val gameInvitation = gameIdFromDeepLink ?: preferenceManager.loadGameInvitation()
        return gameInvitation?.let {
            observeGame(it).take(1)
                .compose(schedulerProvider.applyDefault())
                .doOnTerminate { preferenceManager.saveGameInvitation(null) }
                .map { gameUpdate ->
                    if (gameUpdate.game.state == GameState.Finished) {
                        Result.GameFinished(gameUpdate.game.name)
                    } else {
                        Result.DeepLinkExists(gameUpdate.game)
                    }
                }
        } ?: Observable.just(Result.NoDeepLink)
    }

    private fun logout() {
        view.finishScreen()
        authenticator.logout()
    }

    private fun joinGameAndGoToAddCards(game: Game) {
        getMyUser().take(1)
            .compose(schedulerProvider.applyDefault())
            .switchMapCompletable { joinGame(game, it) }
            .subscribe({
                if (game.state == GameState.Finished) {
                    view.navigateToGameOver(game.id)
                } else {
                    view.navigateToAddCards()
                }
            }, {
                Timber.e(it, "error joinGame")
                view.showGenericError()
            }).let { disposables.add(it) }
    }

    private fun fetchGames() {
        getGames()
            .compose(schedulerProvider.applyDefault())
            .doOnSubscribe { view.showLoading(true) }
            .subscribe(
                { games ->
                    view.showLoading(false)
                    view.showNoGamesView(games.isEmpty())
                    view.show(games)
                    if (games.isEmpty() && !preferenceManager.wasHelpAlreadyShown()){
                        view.showHelp()
                        preferenceManager.setHelpAlreadyShown(true)
                    }
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

    fun onUserJoinGameResponse(game: Game, approved: Boolean) {
        if (approved) {
            joinGameAndGoToAddCards(game)
        } else {
            fetchGames()
        }
    }

    interface View {
        fun show(games: List<Game>)
        fun showGenericError()
        fun showNeedLoginInfo()
        fun showApproveJoinGame(game: Game)
        fun showLoading(show: Boolean)
        fun finishScreen()
        fun navigateToAddCards()
        fun showJoinedGameIsFinished(gameName: String)
        fun showNoGamesView(visible: Boolean)
        fun navigateToGameOver(gameId: String)
        fun showHelp()
    }

    sealed class Result {
        data class GameFinished(val gameName: String) : Result()
        object NoDeepLink : Result()
        data class DeepLinkExists(val game: Game) : Result()
    }
}
