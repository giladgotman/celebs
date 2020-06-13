package com.gggames.celebs.model

data class Player(
    val id: String,
    val name: String,
    val team: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return if (other is Player) {
            this.id == other.id
        } else false
    }
}


