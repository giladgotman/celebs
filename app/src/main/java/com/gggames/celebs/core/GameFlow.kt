package com.gggames.celebs.core

import com.gggames.celebs.data.PlayersRepositoryImpl
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.JoinGame
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import timber.log.Timber

object GameFlow {

    private lateinit var joinGame: JoinGame

    var currentGame: Game? = null
        private set

    fun joinAGame(player: Player, game: Game) {
        joinGame = JoinGame(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        currentGame = game
        joinGame(game.id, player).subscribe({
            Timber.w("ggg you joined game : ${game.id}")
        },{
            Timber.e(it,"ggg failed to  join game : ${game.id}")
        })
    }
}