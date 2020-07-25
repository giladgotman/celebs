package com.gggames.celebs.features.games.data.remote

import com.gggames.celebs.common.factory.createGame
import com.gggames.celebs.model.GameState
import org.junit.Before
import org.junit.Test

class FirebaseGamesDataSourceFakeTest {

    private val dataSourceFake = FirebaseGamesDataSourceFake()

    @Before
    fun setUp() {
    }

    @Test
    fun `dont emit if there is no game`() {
        val observer = dataSourceFake.observeGame("id").test()
        observer.assertNoValues()
    }

    @Test
    fun `emit if there is a game`() {
        val id = "id"
        val game = createGame(id = id)
        dataSourceFake.setGame(game).blockingAwait()
        val observer = dataSourceFake.observeGame(id).test()
        observer.assertValue(game)
    }

    @Test
    fun `emit if there was an update to a game`() {
        val id = "id"
        val game = createGame(id = id)
        val updatedGame = game.copy(name = "updated")
        dataSourceFake.setGame(game).blockingAwait()
        val observer = dataSourceFake.observeGame(id).test()
        observer.assertValueAt(0, game)
        dataSourceFake.setGame(updatedGame).blockingAwait()
        observer.assertValueAt(1, updatedGame)
    }

    @Test
    fun `Given game is set When games requested with gameId Then game is returned`() {
        val game = createGame(id = "id", state = GameState.Started)
        dataSourceFake.games = mutableListOf(game)
        val observer = dataSourceFake.getGames(listOf(game.id), listOf(GameState.Started)).test()
        observer.assertValueAt(0, listOf(game))
    }
}
