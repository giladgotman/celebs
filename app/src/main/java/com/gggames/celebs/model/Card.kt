package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(val id: String? = null, val name: String, val player: String, val used: Boolean = false) :
    Parcelable