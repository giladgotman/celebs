package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*


fun CardRaw.toUi() = Card(this.name)

fun PlayerRaw.toUi() = Player(this.id, this.name)

fun GroupRaw.toUi() = Group(this.name, this.players.map { it.toUi() })

fun RoundRaw.toUi() = Round.Speaking // // TODO: 05.04.20 convert to real round

fun GameStateRaw.toUi() = when (this.state) {
    "created" -> GameState.Created(
        this.myCards.map { it.toUi() },
        this.otherCardsCount)
    "empty" -> GameState.Empty
    else -> TODO()
}

fun GameRaw.toUi() = Game(
    this.id,
    this.name,
    this.createdAt.toDate().time,
    this.celebsCount.toInt(),
    this.groups.map { it.toUi() },
    this.rounds.map {
        it.toUi()
    },
    this.state.toUi()
)


