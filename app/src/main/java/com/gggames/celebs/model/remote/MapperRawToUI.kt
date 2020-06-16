package com.gggames.celebs.model.remote

import com.gggames.celebs.model.*


fun CardRaw.toUi() = Card(
    this.id,
    this.name,
    this.player,
    this.used
)

fun PlayerRaw.toUi() = Player(this.id, this.name, this.team)

fun TeamRaw.toUi() = Team(name = this.name, score = this.score)

fun GameRaw.toUi() = Game(
    this.id,
    this.name,
    this.createdAt.toDate().time,
    this.password,
    this.celebsCount.toInt(),
    this.teams.map { it.toUi() },
    GameState.fromName(this.state),
    this.gameInfo.toUi(),
    this.host.toUi()
)

fun GameInfoRaw.toUi() = GameInfo(
    this.totalCards, this.cardsInDeck, this.round.toUi()
)

fun RoundRaw.toUi() = Round(
    RoundState.fromName(this.roundState), this.roundNumber, this.turn.toUi()
)


fun TurnRaw.toUi() = Turn(
    TurnState.fromName(this.state), this.player?.toUi(), this.time, this.cardsFound
)




