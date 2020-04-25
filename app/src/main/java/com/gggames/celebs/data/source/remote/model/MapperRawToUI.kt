package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*


fun CardRaw.toUi() = Card(this.id, this.name, this.player, this.used)

fun PlayerRaw.toUi() = Player(this.id, this.name, this.team)

fun TeamRaw.toUi() = Team(this.name, this.players.map { it.toUi() })

fun GameStateRaw.toUi() = when (this.state) {
    "created" -> GameState.Created(
        this.myCards.map { it.toUi() },
        this.otherCardsCount)
    "empty" -> GameState.Empty
    "ready" -> TODO()
    "started" -> GameState.Started(this.gameInfo.toUi())
    "finished" -> TODO()
    else -> TODO()
}

fun GameRaw.toUi() = Game(
    this.id,
    this.name,
    this.createdAt.toDate().time,
    this.celebsCount.toInt(),
    this.teams.map { it.toUi() },
    this.players.map { it.toUi() },
    this.state.toUi()
)

fun GameInfoRaw.toUi() = GameInfo(
    this.round, this.score, this.totalCards, this.cardsInDeck, this.currentPlayer?.toUi()
)


