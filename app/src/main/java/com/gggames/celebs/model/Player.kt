package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(
    val id: String,
    val name: String,
    val team: String? = null,
    val games: List<String> = emptyList()
) : Parcelable
