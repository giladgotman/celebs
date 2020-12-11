package com.gggames.hourglass.presentation.gameon

import androidx.test.platform.app.InstrumentationRegistry
import com.gggames.hourglass.core.di.TestDependenciesRule
import com.gggames.hourglass.features.games.domain.SetGame
import factory.createGame
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class GamePresenterMVITest {

    @Inject
    lateinit var tested: GamePresenterMVI

    @Inject
    lateinit var setGame: SetGame

    @get:Rule
    var rule = TestDependenciesRule(InstrumentationRegistry.getInstrumentation().targetContext)

    val uiEvents = PublishSubject.create<GameScreenContract.UiEvent>()

    @Before
    fun setUp() {
        rule.testApplicationComponent.inject(this)
    }

    @Test
    fun goodWeatherFlow() {
        setGame(createGame()).blockingSubscribe()
        tested.bind(uiEvents)


    }
    @After
    fun tearDown() {

    }
}

