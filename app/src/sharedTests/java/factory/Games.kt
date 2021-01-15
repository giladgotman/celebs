package factory

import com.gggames.hourglass.model.*

fun createGame(
    id: String = "id",
    name: String = "name",
    createdAt: Long = 0,
    password: String? = null,
    celebsCount: Int = 6,
    teams: List<Team> = emptyList(),
    state: GameState? = null,
    gameInfo: GameInfo = GameInfo(),
    host: Player = Player("$id.player", "$id.name"),
    type: GameType = GameType.Normal,
    timestamp: Long = 0
) = Game(
    id,
    name,
    createdAt,
    password,
    celebsCount,
    teams,
    state,
    gameInfo,
    host,
    type,
    timestamp
)



fun createGameInfo(
    totalCards: Int = 0,
    cardsInDeck: Int = 0,
    round: Round = Round()

) = GameInfo(
    totalCards = totalCards,
    cardsInDeck = cardsInDeck,
    round = round
)
