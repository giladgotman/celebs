package com.gggames.celebs.core

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.gggames.celebs.core.di.AppContext
import com.gggames.celebs.features.players.domain.JoinGame
import com.gggames.celebs.model.Game
import com.gggames.celebs.model.Player
import com.gggames.celebs.presentation.login.LoginActivity
import com.gggames.celebs.utils.prefs.PreferenceManager
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameFlow @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val joinGame: JoinGame,
    @AppContext private val appContext: Context

){
    var me: Player? = null
        private set
        get() {
            return if (field != null) field
            else preferenceManager.loadPlayer()
        }

    private val disposables = CompositeDisposable()

    var currentGame: Game? = null
        private set

    fun joinAGame(playerName: String, game: Game) {
        currentGame = game
        joinGame(game.id, me!!).subscribe({
            Timber.w("ggg you joined game : ${game.id}")
        },{
            Timber.e(it,"ggg failed to  join game : ${game.id}")
        }).let { disposables.add(it) }
    }

    private fun generatePlayerId(playerName: String, now: Long) = "${playerName}_$now"

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

    fun updateGame(game: Game) {
        currentGame = game
    }

    fun isActivePlayer(): Boolean {
        return currentGame?.gameInfo?.round?.turn?.player == me
    }

    fun setMyTeam(teamName: String) {
        me = me?.copy(team = teamName)
    }
}