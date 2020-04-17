package com.gggames.celebs.domain

import com.gggames.celebs.data.GamesRepository
import com.gggames.celebs.data.model.*
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class AddGame(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game = createDummyGame()): Completable =
        gamesRepository.addGame(game)
            .compose(schedulerProvider.applyDefault())

    fun createDummyGame(): Game =
        Game(id = "gameTestId2",
            name = "testGame2",
            createdAt = System.currentTimeMillis(),
            teams = listOf(
                Team(("group1"),
                    players = emptyList()
                )
            )
            , state = GameState.Created(listOf(Card("Putin", "gilad")), mapOf(createDummyPlayer().id to 5)) )

    private fun createDummyPlayer(name: String = "gilad") = Player(name, name)
}