package com.gggames.hourglass.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val name: String,
    val playerIds: List<String> = emptyList(),
    val score: Int = 0,
    val lastPlayerId: String? = null
) : Parcelable
