package com.gggames.celebs.utils.prefs

import android.content.SharedPreferences
import com.gggames.celebs.model.Player
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class PreferenceManager @Inject constructor(
    private val defaultSharedPreferences: SharedPreferences
) {
    private val PREFS_KEY_PLAYER = "PREFS_KEY_PLAYER"
    private val PREFS_KEY_GAME_INVITATION = "PREFS_KEY_GAME_INVITATION"

    private val gson = Gson()

    fun savePlayer(player: Player?) {
        Timber.v("savePlayer : $player")
        val pj = player?.let { gson.toJson(player) }
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_PLAYER, pj)
        editor.apply()
    }

    fun loadPlayer(): Player? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_PLAYER, null)
        val player = playerString?.let { gson.fromJson(playerString, Player::class.java) }
        Timber.v("loadPlayer : $player")
        return player
    }

    fun saveGameInvitation(gameId: String?) {
        Timber.v("saveGameInvitation : $gameId")
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_GAME_INVITATION, gameId)
        editor.apply()
    }

    fun loadGameInvitation(): String? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_GAME_INVITATION, null)
        Timber.v("loadGameInvitation : $playerString")
        return playerString
    }
}
