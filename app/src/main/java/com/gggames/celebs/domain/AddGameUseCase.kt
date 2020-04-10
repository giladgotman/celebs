package com.gggames.celebs.domain

import com.gggames.celebs.data.GamesRepository
import com.gggames.celebs.data.model.*
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import timber.log.Timber

class AddGameUseCase(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(game: Game = createDummyGame()) =
        gamesRepository.addGame(game)
            .compose(schedulerProvider.applyDefault())
            .subscribe(
                {
                    Timber.i("gilad game added: ${game.id}")
                }, {
                    Timber.e(it,"gilad game added failed. ${it.localizedMessage}")
                })

    fun createDummyGame(): Game =
        Game(id = "gameTestId2",
            name = "testGame2",
            createdAt = System.currentTimeMillis(),
            groups = listOf(
                Group(("group1"),
                    players = emptyList()
                )
            )
            , state = GameState.Created(listOf(Card("Putin")), mapOf(createDummyPlayer().id to 5)) )

    private fun createDummyPlayer(name: String = "gilad") = Player(name, name)
}