package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team (val name: String, val players: List<Player>) : Parcelable


