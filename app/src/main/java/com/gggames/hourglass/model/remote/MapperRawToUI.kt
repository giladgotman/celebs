package com.gggames.hourglass.model.remote

import com.gggames.hourglass.model.*

fun CardRaw.toUi() = Card(
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

fun PlayerRaw.toUi() = Player(this.id, this.name, this.team, this.games, PlayerTurnState.fromName(this.playerTurnState))

fun UserRaw.toUi() = when (this.type) {
    UserType.Guest -> User.Guest(this.id, this.name)
    UserType.LoggedIn -> User.LoggedIn(this.id, this.name, this.games.map { it.toUi() })
}

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
    this.host.toUi(),
    this.type.toGameTypeUi()
)

fun String.toGameTypeUi() = when (this) {
    "Normal" -> GameType.Normal
    "Gift" -> GameType.Gift
    else -> GameType.Normal
}

fun GameInfoRaw.toUi() = GameInfo(
    this.totalCards, this.cardsInDeck, this.round.toUi()
)

fun RoundRaw.toUi() = Round(
    RoundState.fromName(this.roundState), this.roundNumber, this.turn.toUi()
)

fun TurnRaw.toUi() = Turn(
    TurnState.fromName(this.state),
    this.player?.toUi(),
    this.time,
    this.cardsFound,
    this.lastFoundCard?.toUi(),
    this.currentCard?.toUi()
)
