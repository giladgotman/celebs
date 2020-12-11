package com.gggames.hourglass.utils.prefs

import com.gggames.hourglass.model.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManagerFake @Inject constructor() : PreferenceManager {

    override fun savePlayer(player: Player?) {
    }

    override fun loadPlayer(): Player? = null


    override fun saveGameInvitation(gameId: String?) {
    }

    override fun loadGameInvitation(): String? = null
    override fun wasHelpAlreadyShown() = false

    override fun setHelpAlreadyShown(shown: Boolean) {
    }


}