package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(val id: String? = null,
                val name: String,
                val player: String,
                val used: Boolean = false,
                val videoUrl1: String? = null,
                val videoUrl2: String? = null,
                val videoUrl3: String? = null,
                val videoUrlFull: String? = null
): Parcelable