package com.gggames.celebs.core

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.gggames.celebs.data.PlayersRepositoryImpl
import com.gggames.celebs.data.model.Game
import com.gggames.celebs.data.model.Player
import com.gggames.celebs.data.source.remote.FirebasePlayersDataSource
import com.gggames.celebs.domain.ChooseTeam
import com.gggames.celebs.domain.JoinGame
import com.gggames.celebs.presentation.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

object GameFlow {

    lateinit var preferenceManager: PreferenceManager
    private lateinit var joinGame: JoinGame
    private lateinit var chooseTeam: ChooseTeam
    private lateinit var appContext: Context
    var me: Player? = null
        private set
        get() {
            return if (field != null) field
            else preferenceManager.loadPlayer()
        }

    private val disposables = CompositeDisposable()

    var currentGame: Game? = null
        private set

    fun setContext(context: Context) {
        appContext = context
        preferenceManager = PreferenceManager(context)
    }

    fun joinAGame(playerName: String, game: Game) {
        joinGame = JoinGame(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        chooseTeam = ChooseTeam(
            PlayersRepositoryImpl(
                FirebasePlayersDataSource(
                    FirebaseFirestore.getInstance()
                )
            ),
            SchedulerProvider()
        )

        currentGame = game
        joinGame(game.id, me!!).subscribe({
            Timber.w("ggg you joined game : ${game.id}")
        },{
            Timber.e(it,"ggg failed to  join game : ${game.id}")
        }).let { disposables.add(it) }
    }

    private fun generatePlayerId(playerName: String, now: Long) = "${playerName}_$now"

    fun chooseAteam(teamName: String) {
        currentGame?.let {
            chooseTeam(it.id, me!!, teamName)
                .subscribe({
                Timber.w("ggg you chosed team : $teamName")
            },{
                Timber.e(it,"ggg failed to choose team : $teamName")
            }).let { disposables.add(it) }
        }
    }

    fun logout() {
        me = null
        preferenceManager.savePlayer(null)
        val intent = Intent(appContext, LoginActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun clear() {
        disposables.clear()
    }

    fun login(playerName: String) {
        val now = System.currentTimeMillis()
        val id = generatePlayerId(playerName, now)
        me = Player(id, playerName)
        preferenceManager.savePlayer(me)
    }
}