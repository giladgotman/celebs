package com.gggames.hourglass.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val name: String,
    val players: List<Player> = emptyList(), // used only locally
    val score: Int = 0,
    val lastPlayerId: String? = null
) : Parcelable
