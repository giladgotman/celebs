package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*
import com.google.firebase.Timestamp
import java.util.*


fun Card.toRaw() = CardRaw(this.name)

fun Player.toRaw() = PlayerRaw(this.name)

fun Group.toRaw() = GroupRaw(this.name, this.players.map { it.toRaw() })

fun Round.toRaw() = RoundRaw(this.name)

fun GameState.toRaw() = when (this) {
    is GameState.Created -> GameStateRaw(
        "created",
        this.myCards.map { it.toRaw() },
        this.otherCardsCount.mapKeys { it.key.toRaw() })
    is GameState.Ready -> TODO()
    is GameState.Started -> TODO()
    is GameState.Finished -> TODO()
}

fun Game.toRaw() = GameRaw(
    this.id,
    this.name,
    Timestamp(Date(this.createdAt)),
    this.celebsCount.toLong(),
    this.groups.map { it.toRaw() },
    this.rounds.map {
        it.toRaw()
    },
    this.state.toRaw()
)


