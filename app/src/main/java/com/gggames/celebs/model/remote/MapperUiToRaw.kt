package com.gggames.celebs.model.remote

import com.gggames.celebs.model.*
import com.google.firebase.Timestamp
import java.util.*


fun Card.toRaw() = CardRaw(
    this.id,
    this.name,
    this.player,
    this.used
)

fun Player.toRaw() =
    PlayerRaw(this.id, this.name, this.team)

fun Team.toRaw() = TeamRaw(
    this.name,
    this.players.map { it.toRaw() })

fun Game.toRaw() = GameRaw(
    this.id,
    this.name,
    Timestamp(Date(this.createdAt)),
    this.password,
    this.celebsCount.toLong(),
    this.teams.map { it.toRaw() },
    this.state?.name,
    this.gameInfo.toRaw()
)

fun Round.toRaw() = RoundRaw(
    this.state.toRaw(), this.roundNumber, this.turn.toRaw()
)

fun Turn.toRaw() = TurnRaw(
    this.state.toRaw(), this.player?.toRaw(), this.time
)
fun RoundState.toRaw() = this.name

fun TurnState.toRaw() = this.name


fun GameInfo.toRaw() = GameInfoRaw(
    this.score,
    this.totalCards,
    this.cardsInDeck,
    this.round.toRaw()
)

fun GameState.toRaw() = this.name


