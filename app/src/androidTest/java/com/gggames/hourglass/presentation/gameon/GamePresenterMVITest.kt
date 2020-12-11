package com.gggames.hourglass.presentation.gameon

import androidx.test.platform.app.InstrumentationRegistry
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.core.di.DaggerTestAppComponent
import com.gggames.hourglass.core.di.TestAppModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class GamePresenterMVITest {


    @Inject
    lateinit var tested : GamePresenterMVI

    @Before
    fun setUp() {

        val app = InstrumentationRegistry.getInstrumentation().context.applicationContext as CelebsApplication
        val testAppComponent = DaggerTestAppComponent.builder()
            .testAppModule(TestAppModule(app))
            .build()


    }


    @Test
    fun goodWeatherFlow() {
        val res = tested.unBind()
    }
    @After
    fun tearDown() {

    }
}

