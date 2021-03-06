package com.gggames.hourglass.model.remote

import com.gggames.hourglass.model.*
import com.google.firebase.Timestamp
import java.util.*

fun Card.toRaw() = CardRaw(
    this.id,
    this.name,
    this.player,
    this.used,
    this.index,
    this.videoUrl1,
    this.videoUrl2,
    this.videoUrl3,
    this.videoUrlFull
)

fun Player.toRaw() =
    PlayerRaw(this.id, this.name, this.team, this.games, this.playerTurnState?.name ?: PlayerTurnState.Idle.name)

fun User.toRaw() = when (this) {
    is User.Guest -> UserRaw(id = this.id, name = this.name, type = UserType.Guest)
    is User.LoggedIn -> UserRaw(id = this.id, name = this.name, type = UserType.LoggedIn)
}

fun Team.toRaw() = TeamRaw(
    this.name,
    this.playerIds,
    this.score,
    lastPlayerId = this.lastPlayerId
)

fun Game.toRaw() = GameRaw(
    this.id,
    this.name,
    Timestamp(Date(this.createdAt)),
    this.password,
    this.celebsCount.toLong(),
    this.teams.map { it.toRaw() },
    this.state?.name,
    this.gameInfo.toRaw(),
    this.host.toRaw(),
    this.type.name
)

fun Round.toRaw() = RoundRaw(
    this.state.toRaw(), this.roundNumber, this.turn.toRaw()
)

fun Turn.toRaw() = TurnRaw(
    this.state.toRaw(),
    this.player?.toRaw(),
    this.nextPlayer?.toRaw(),
    this.time,
    this.cardsFound,
    this.lastFoundCard?.toRaw(),
    this.currentCard?.toRaw()
)
fun RoundState.toRaw() = this.name

fun TurnState.toRaw() = this.name

fun GameInfo.toRaw() = GameInfoRaw(
    this.totalCards,
    this.cardsInDeck,
    this.round.toRaw()
)

fun GameState.toRaw() = this.name
