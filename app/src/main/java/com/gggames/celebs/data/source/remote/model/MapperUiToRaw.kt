package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*
import com.google.firebase.Timestamp
import java.util.*


fun Card.toRaw() = CardRaw(this.id, this.name, this.player, this.used)

fun Player.toRaw() = PlayerRaw(this.id, this.name, this.team)

fun Team.toRaw() = TeamRaw(this.name, this.players.map { it.toRaw() })

fun GameState.toRaw() = when (this) {
    is GameState.Empty -> GameStateRaw("empty", gameInfo = this.gameInfo.toRaw())
    is GameState.Ready -> GameStateRaw("ready", gameInfo = this.gameInfo.toRaw())
    is GameState.Started -> GameStateRaw("started", gameInfo = this.gameInfo.toRaw())
    is GameState.Finished -> GameStateRaw("finished", gameInfo = this.gameInfo.toRaw())
}

fun Game.toRaw() = GameRaw(
    this.id,
    this.name,
    Timestamp(Date(this.createdAt)),
    this.celebsCount.toLong(),
    this.teams.map { it.toRaw() },
    this.players.map { it.toRaw() },
    this.state.toRaw()
)

fun GameInfo.toRaw() = GameInfoRaw(
    this.round,
    this.score,
    this.totalCards,
    this.cardsInDeck,
    this.currentPlayer?.toRaw()
)


