package com.gggames.celebs.core

import android.content.SharedPreferences
import com.gggames.celebs.data.model.Player
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val defaultSharedPreferences: SharedPreferences)
{
    private val PREFS_KEY_PLAYER = "PREFS_KEY_PLAYER"

    private val gson  = Gson()

    fun savePlayer(player: Player?) {
        Timber.w("savePlayer : $player")
        val pj = player?.let { gson.toJson(player) }
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_PLAYER, pj)
        editor.apply()
    }

    fun loadPlayer(): Player? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_PLAYER, null)
        val player = playerString?.let { gson.fromJson(playerString, Player::class.java)}
        Timber.w("loadPlayer : $player")
        return player
    }



}