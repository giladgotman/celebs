package com.gggames.hourglass.utils.prefs

import com.gggames.hourglass.features.user.data.remote.fakePlayer
import com.gggames.hourglass.model.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManagerFake @Inject constructor() : PreferenceManager {
    var wasHelpShown = false
    var player: Player? = fakePlayer

    override fun savePlayer(player: Player?) {
        this.player = player
    }

    override fun loadPlayer(): Player? = player


    override fun saveGameInvitation(gameId: String?) {
    }

    override fun loadGameInvitation(): String? = null
    override fun wasHelpAlreadyShown() = wasHelpShown

    override fun setHelpAlreadyShown(shown: Boolean) {
        wasHelpShown = shown
    }


}