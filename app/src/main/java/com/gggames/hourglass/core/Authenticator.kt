package com.gggames.hourglass.core

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.gggames.hourglass.core.di.AppContext
import com.gggames.hourglass.model.Game
import com.gggames.hourglass.model.Player
import com.gggames.hourglass.presentation.login.SignupActivity
import com.gggames.hourglass.utils.prefs.PreferenceManager
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Authenticator @Inject constructor(
    private val preferenceManager: PreferenceManager,
    @AppContext private val appContext: Context

) {
    // TODO: 12.07.20 move it to user repo
    var me: Player? = null
        private set
        get() {
            return if (field != null) field
            else preferenceManager.loadPlayer()
        }

    // at this point the user id is the same as its username
    private fun generatePlayerId(username: String) = username

    fun logout() {
        me = null
        preferenceManager.savePlayer(null)
        val intent = Intent(appContext, SignupActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun signup(username: String) =
        Completable.fromCallable {
            val id = generatePlayerId(username)
            me = Player(id, username)
            preferenceManager.savePlayer(me)
        }

    fun isMyselfActivePlayerBlocking(game: Game) = me?.id == game.currentPlayer?.id

    fun isMyselfActivePlayer(game: Game) = Single.fromCallable {
        isMyselfActivePlayerBlocking(game)
    }

    fun isMyselfHost(game: Game) = me?.id == game.host.id

    fun setMyTeam(teamName: String) {
        me = me?.copy(team = teamName)
    }
}
