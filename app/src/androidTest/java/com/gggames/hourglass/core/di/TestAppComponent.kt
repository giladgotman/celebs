package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.video.VideoModule
import com.gggames.hourglass.presentation.gameon.GamePresenterMVITest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestAppModule::class,
    SubComponentsModule::class,
    NetworkModule::class,
    FakeGamesModule::class,
    FakeCardsModule::class,
    FakePlayersModule::class,
    FakeUserModule::class,
    VideoModule::class])
interface TestAppComponent: AppComponent {
    fun inject(app: GamePresenterMVITest)
}
