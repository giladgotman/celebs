package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@Parcelize
data class Player(
    val id: String,
    val name: String,
    val team: String? = null,
    val games: List<String> = emptyList(),
    val playerTurnState: PlayerTurnState? = PlayerTurnState.Idle

) : Parcelable


enum class PlayerTurnState {
    Idle,
    Playing,
    UpNext;

    companion object {
        fun fromName(name: String?): PlayerTurnState =
            when (name) {
                "Idle" -> Idle
                "Playing" -> Playing
                "UpNext" -> UpNext
                else -> {
                    Timber.w("Unknown player turn state name: $name , setting state to Idle")
                    Idle
                }
            }
    }
}