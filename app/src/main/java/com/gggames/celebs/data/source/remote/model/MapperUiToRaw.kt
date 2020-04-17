package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*
import com.google.firebase.Timestamp
import java.util.*


fun Card.toRaw() = CardRaw(this.name)

fun Player.toRaw() = PlayerRaw(this.id, this.name, this.team)

fun Team.toRaw() = TeamRaw(this.name, this.players.map { it.toRaw() })

fun Round.toRaw() = RoundRaw(this.name)

fun GameState.toRaw() = when (this) {
    is GameState.Empty -> GameStateRaw("empty")
    is GameState.Created -> GameStateRaw(
        "created",
        this.myCards.map { it.toRaw() },
        this.otherCardsCount
    )
    is GameState.Ready -> TODO()
    is GameState.Started -> TODO()
    is GameState.Finished -> TODO()
}

fun Game.toRaw() = GameRaw(
    this.id,
    this.name,
    Timestamp(Date(this.createdAt)),
    this.celebsCount.toLong(),
    this.teams.map { it.toRaw() },
    this.rounds.map {
        it.toRaw()
    },
    this.players.map { it.toRaw() },
    this.state.toRaw(),
    this.cards.map { it.toRaw() }
)


