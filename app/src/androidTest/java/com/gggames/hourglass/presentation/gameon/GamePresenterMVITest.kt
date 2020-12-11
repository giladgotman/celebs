package com.gggames.hourglass.presentation.gameon

import androidx.test.platform.app.InstrumentationRegistry
import com.gggames.hourglass.core.di.TestDependenciesRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class GamePresenterMVITest {

    @Inject
    lateinit var tested: GamePresenterMVI

    @get:Rule
    var rule = TestDependenciesRule(InstrumentationRegistry.getInstrumentation().targetContext)

    @Before
    fun setUp() {
        rule.testApplicationComponent.inject(this)
    }

    @Test
    fun goodWeatherFlow() {
        val res = tested.unBind()
    }
    @After
    fun tearDown() {

    }
}

