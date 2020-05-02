package com.gggames.celebs.data.source.remote.model

import com.gggames.celebs.data.model.*


fun CardRaw.toUi() = Card(this.id, this.name, this.player, this.used)

fun PlayerRaw.toUi() = Player(this.id, this.name, this.team)

fun TeamRaw.toUi() = Team(this.name, this.players.map { it.toUi() })

//fun GameStateRaw.toUi() = when (this.state) {
//    "empty" -> GameState.Empty(this.gameInfo.toUi())
//    "ready" -> GameState.Ready(this.gameInfo.toUi())
//    "started" -> GameState.Started(this.gameInfo.toUi())
//    "finished" -> GameState.Finished(this.gameInfo.toUi())
//    else -> throw IllegalArgumentException("Unknown state: ${this.state}")
//}

fun GameRaw.toUi() = Game(
    this.id,
    this.name,
    this.createdAt.toDate().time,
    this.celebsCount.toInt(),
    this.teams.map { it.toUi() },
    GameStateE.fromName(this.state),
    this.gameInfo.toUi()

)

fun GameInfoRaw.toUi() = GameInfo(
    this.round, this.score, this.totalCards, this.cardsInDeck, this.currentPlayer?.toUi()
)



