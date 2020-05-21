package com.gggames.celebs.model.remote

import com.gggames.celebs.model.*


fun CardRaw.toUi() = Card(
    this.id,
    this.name,
    this.player,
    this.used
)

fun PlayerRaw.toUi() = Player(this.id, this.name, this.team)

fun TeamRaw.toUi() = Team(this.name, this.players.map { it.toUi() })

fun GameRaw.toUi() = Game(
    this.id,
    this.name,
    this.createdAt.toDate().time,
    this.celebsCount.toInt(),
    this.teams.map { it.toUi() },
    GameState.fromName(this.state),
    this.gameInfo.toUi()

)

fun GameInfoRaw.toUi() = GameInfo(
    this.score, this.totalCards, this.cardsInDeck, this.round.toUi()
)

fun RoundRaw.toUi() = Round(
    RoundState.fromName(this.roundState), this.roundNumber, this.turn.toUi()
)


fun TurnRaw.toUi() = Turn(
    TurnState.fromName(this.state), this.player?.toUi(), this.time
)




