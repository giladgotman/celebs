package com.gggames.celebs.core

import android.content.Context
import com.gggames.celebs.R
import com.gggames.celebs.data.model.Player
import com.google.gson.Gson

class PreferenceManager(context: Context) {

    private val PREFS_KEY_PLAYER = "PREFS_KEY_PLAYER"
    private val defaultSharedPreferences = context.getSharedPreferences(
        context.getString(R.string.shared_prefs_default), Context.MODE_PRIVATE
    )

    private val gson  = Gson()

    fun savePlayer(player: Player?) {
        val pj = player?.let { gson.toJson(player) }
        val editor = defaultSharedPreferences.edit()
        editor.putString(PREFS_KEY_PLAYER, pj)
        editor.apply()
    }

    fun loadPlayer(): Player? {
        val playerString = defaultSharedPreferences.getString(PREFS_KEY_PLAYER, null)
        return playerString?.let { gson.fromJson(playerString, Player::class.java)}
    }



}