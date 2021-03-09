package com.gggames.hourglass.utils.prefs

import android.content.SharedPreferences
import com.gggames.hourglass.model.Player
import com.google.gson.Gson
import timber.log.Timber

interface PreferenceManager {
    fun savePlayer(player: Player?)
    fun loadPlayer(): Player?
    fun saveGameInvitation(gameId: String?)
    fun loadGameInvitation(): String?
    fun wasHelpAlreadyShown(): Boolean
    fun setHelpAlreadyShown(shown: Boolean)
}

class PreferenceManagerReal constructor(
    private val defaultSharedPreferences: SharedPreferences
): PreferenceManager {
    private val PREFS_KEY_PLAYER = "PREFS_KEY_PLAYER"
    private val PREFS_KEY_GAME_INVITATION = "PREFS_KEY_GAME_INVITATION"
    private val PREFS_KEY_HELP_SHOWN = "PREFS_KEY_IS_FIRST_LAUNCH"

    private val gson = Gson()

    override fun savePlayer(player: Player?) {
        Timber.v("savePlayer : $player")
        val pj = player?.let { gson.toJson(player) }
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_PLAYER, pj)
        editor.apply()
    }

    override fun loadPlayer(): Player? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_PLAYER, null)
        val player = playerString?.let { gson.fromJson(playerString, Player::class.java) }
        Timber.v("loadPlayer : $player")
        return player
    }

    override fun saveGameInvitation(gameId: String?) {
        Timber.v("saveGameInvitation : $gameId")
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_GAME_INVITATION, gameId)
        editor.apply()
    }

    override fun loadGameInvitation(): String? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_GAME_INVITATION, null)
        Timber.v("loadGameInvitation : $playerString")
        return playerString
    }

    override fun wasHelpAlreadyShown(): Boolean {
        return defaultSharedPreferences.getBoolean(PREFS_KEY_HELP_SHOWN, false)
    }

    override fun setHelpAlreadyShown(shown: Boolean) {
        val editor = defaultSharedPreferences.edit()
        editor.putBoolean(PREFS_KEY_HELP_SHOWN, shown)
        editor.apply()
    }
}
