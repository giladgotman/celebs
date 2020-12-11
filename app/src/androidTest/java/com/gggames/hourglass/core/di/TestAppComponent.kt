package com.gggames.hourglass.core.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [GamesModule::class])
interface TestAppComponent: AppComponent
