package com.gggames.celebs.utils.prefs

import com.gggames.celebs.features.user.data.remote.fakePlayer
import com.gggames.celebs.model.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManagerFake @Inject constructor() : PreferenceManager {
    override fun savePlayer(player: Player?) {
    }

    override fun loadPlayer(): Player? =
        fakePlayer


    override fun saveGameInvitation(gameId: String?) {
    }

    override fun loadGameInvitation(): String? = null


}