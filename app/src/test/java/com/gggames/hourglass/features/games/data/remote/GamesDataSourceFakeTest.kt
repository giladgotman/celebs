package com.gggames.hourglass.features.games.data.remote

import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.model.GameState
import factory.createGame
import org.junit.Before
import org.junit.Test

class GamesDataSourceFakeTest {

    private val dataSourceFake = GamesDataSourceFake()

    @Before
    fun setUp() {
    }

    @Test
    fun `dont emit if there is no game`() {
        dataSourceFake.games = mutableListOf()
        val observer = dataSourceFake.observeGame("id").test()
        observer.assertNoValues()
    }

    @Test
    fun `emit if there is a game`() {
        val id = "id"
        val game = createGame(id = id)
        dataSourceFake.setGame(game).blockingAwait()
        val observer = dataSourceFake.observeGame(id).test()
        observer.assertValue(GameResult.Found(game))
    }

    @Test
    fun `emit if there was an update to a game`() {
        val id = "id"
        val game = createGame(id = id)
        val updatedGame = game.copy(name = "updated")
        dataSourceFake.setGame(game).blockingAwait()
        val observer = dataSourceFake.observeGame(id).test()
        observer.assertValueAt(0, GameResult.Found(game))
        dataSourceFake.setGame(updatedGame).blockingAwait()
        observer.assertValueAt(1, GameResult.Found(updatedGame))
    }

    @Test
    fun `Given game is set When games requested with gameId Then game is returned`() {
        val game = createGame(id = "id", state = GameState.Started)
        dataSourceFake.games = mutableListOf(game)
        val observer = dataSourceFake.getGames(listOf(game.id), listOf(GameState.Started)).test()
        observer.assertValueAt(0, listOf(game))
    }
}
