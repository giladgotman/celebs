package com.gggames.hourglass.presentation.gameon

import androidx.test.platform.app.InstrumentationRegistry
import com.gggames.hourglass.core.di.TestDependenciesRule
import com.gggames.hourglass.features.games.domain.SetGame
import com.gggames.hourglass.utils.waitForAllEvents
import com.gggames.hourglass.utils.withFirstValue
import com.google.common.truth.Truth.assertThat
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

    private val uiEvents = PublishSubject.create<GameScreenContract.UiEvent>()

    @Before
    fun setUp() {
        rule.testApplicationComponent.inject(this)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun goodWeatherFlowOnlyBind() {
        setGame(createGame()).blockingSubscribe()
        val states = tested.states.test()
        tested.bind(uiEvents)
        states.waitForAllEvents()
        states.withFirstValue {
            assertThat(it).isEqualTo(GameScreenContract.State.initialState)
        }
        states.assertValueCount(3) // initial ; resetTime = true and PlayButtonState.isEnabled = true ; cardsInDeck
    }

    @Test
    fun userStartsGame() {
        setGame(createGame()).blockingSubscribe()
        val states = tested.states.test()
        tested.bind(uiEvents)
        states.waitForAllEvents()
        states.values().clear()

        uiEvents.onNext(GameScreenContract.UiEvent.StartStopClick(buttonState = GameScreenContract.ButtonState.Stopped, time = 500))
        states.waitForAllEvents()
    }


}

