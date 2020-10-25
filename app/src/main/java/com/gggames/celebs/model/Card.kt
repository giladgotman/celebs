package com.gggames.celebs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(
    val id: String,
    val name: String,
    val player: String,
    val used: Boolean = false,
    val index: Int = 0,
    val videoUrl1: String? = null,
    val videoUrl2: String? = null,
    val videoUrl3: String? = null,
    val videoUrlFull: String? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (id != other.id) return false
        if (name != other.name) return false
        if (player != other.player) return false
        if (used != other.used) return false
        if (index != other.index) return false
        if (videoUrl1 != other.videoUrl1) return false
        if (videoUrl2 != other.videoUrl2) return false
        if (videoUrl3 != other.videoUrl3) return false
        if (videoUrlFull != other.videoUrlFull) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + player.hashCode()
        result = 31 * result + used.hashCode()
        result = 31 * result + index
        result = 31 * result + (videoUrl1?.hashCode() ?: 0)
        result = 31 * result + (videoUrl2?.hashCode() ?: 0)
        result = 31 * result + (videoUrl3?.hashCode() ?: 0)
        result = 31 * result + (videoUrlFull?.hashCode() ?: 0)
        return result
    }
}
