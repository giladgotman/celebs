package com.gggames.celebs.domain

import com.gggames.celebs.data.GamesRepository
import com.gggames.celebs.data.model.*

class AddGameUseCase(
    private val gamesRepository: GamesRepository
){
    operator fun invoke(game: Game = createDummyGame()) = gamesRepository.addGame(game)

    fun createDummyGame(): Game =
        Game(id = "gameTestId", name = "testGame", createdAt = System.currentTimeMillis(), groups = listOf(Group(("group1"), players = listOf(
            createDummyPlayer()))), state = GameState.Created(listOf(Card("Putin")), mapOf(createDummyPlayer() to 5)) )

    private fun createDummyPlayer(name: String = "gilad") = Player(name)
}