package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.video.VideoModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestAppModule::class,
    SubComponentsModule::class,
    NetworkModule::class,
    GamesModule::class,
    CardsModule::class,
    PlayersModule::class,
    UserModule::class,
    VideoModule::class])
interface TestAppComponent: AppComponent
