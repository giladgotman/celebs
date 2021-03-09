package com.gggames.hourglass.features.games.di

import dagger.Subcomponent
import javax.inject.Qualifier

@PerGame
@Subcomponent(modules = [GameModule::class])
interface GameComponent {
    @Subcomponent.Builder
    interface Builder {
        fun gameModule(gameModule: GameModule): Builder
        fun build(): GameComponent
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PerGame
