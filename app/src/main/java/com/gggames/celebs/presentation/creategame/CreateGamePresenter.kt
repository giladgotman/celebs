package com.gggames.celebs.presentation.creategame

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.GameInfo
import com.gggames.celebs.model.GameState
import com.gggames.celebs.model.Team
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CreateGamePresenter @Inject constructor(
    private val setGame: SetGame,
    private val gameFlow: GameFlow,
    private val joinGame: JoinGame
) {

    private lateinit var view: View
    private val disposables = CompositeDisposable()


    fun bind(view: View) {
        this.view = view
    }

    fun onDoneClick(gameDetails: GameDetails) {
        val now = System.currentTimeMillis()
        val initialScore = gameDetails.teams.map { it.name to 0 }.toMap()
        val game = Game(
            "${gameDetails.name}$now",
            gameDetails.name,
            now,
            gameDetails.password,
            gameDetails.cardsCount,
            gameDetails.teams,
            GameState.Created,
            GameInfo(score = initialScore),
            gameFlow.me!!
        )
        joinGame(game)
    }

    private fun joinGame(game: Game) {
        setGame(game)
            .doOnSubscribe {
                view.setDoneEnabled(false)
            }
            .andThen(joinGame(game, gameFlow.me!!))
            .subscribe(
                {
                    view.navigateToAddCards(game.id)
                }, {
                    view.setDoneEnabled(true)
                    Timber.e(it, "game add and join failed.")
                })
            .let {
                disposables.add(it)
            }
    }

    fun unBind() {
        disposables.clear()
    }

    interface View {
        fun setDoneEnabled(enabled: Boolean)
        fun navigateToAddCards(gameId: String)
        fun showGenericError()
    }
}

data class GameDetails(
    val name: String,
    val teams: List<Team>,
    val cardsCount: Int,
    val password: String?
)