package com.gggames.celebs.presentation.creategame

import com.gggames.celebs.features.games.domain.SetGame
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.features.user.domain.GetMyUser
import com.gggames.celebs.model.*
import io.reactivex.Single.just
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CreateGamePresenter @Inject constructor(
    private val setGame: SetGame,
    private val getMyUser: GetMyUser,
    private val joinGame: JoinGame
) {

    private lateinit var view: View
    private val disposables = CompositeDisposable()


    fun bind(view: View) {
        this.view = view
    }

    fun onDoneClick(gameDetails: GameDetails) {
        getMyUser().firstOrError().flatMap {
            val game = createGame(gameDetails, it)
            // TODO: 12.07.20 check if the setGame can be removed. it is used in joinGame (but with updateRemote = false
            setGame(game)
                .andThen(joinGame(game, it).andThen(just(game)))
        }
            .doOnSubscribe {
                view.setDoneEnabled(false)
            }
            .subscribe(
                { game ->
                    view.navigateToAddCards(game.id)
                }, {
                    view.setDoneEnabled(true)
                    Timber.e(it, "game add and join failed.")
                })
            .let {
                disposables.add(it)
            }
    }

    private fun createGame(gameDetails: GameDetails, user: Player): Game {
        val now = System.currentTimeMillis()
        return Game(
            "${gameDetails.name}$now",
            gameDetails.name,
            now,
            gameDetails.password,
            gameDetails.cardsCount,
            gameDetails.teams,
            GameState.Created,
            GameInfo(),
            user
        )
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