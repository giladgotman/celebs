package com.gggames.hourglass.core.di

import android.content.Context
import com.gggames.hourglass.core.CelebsApplication
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Test rule that creates and sets a Dagger TestApplicationComponent into the application overriding the existing application component. Use
 * this rule in your test case in order for the app to use mock dependencies. It also exposes some of the dependencies so they can be easily
 * accessed from the tests, e.g. to stub mocks etc.
 */
class TestDependenciesRule(val targetContext: Context) : TestRule {

    val testApplicationComponent: TestAppComponent

    init {
        val application = this.targetContext.applicationContext as CelebsApplication
        this.testApplicationComponent = DaggerTestAppComponent.builder()
            .testAppModule(
                TestAppModule(application)
            )
            .build()
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val application = CelebsApplication[targetContext]
                application.appComponent = testApplicationComponent
                try {
                    base.evaluate()
                } finally {
                }
            }
        }
    }
}
