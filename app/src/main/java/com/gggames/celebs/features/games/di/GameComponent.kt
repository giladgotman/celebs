package com.gggames.celebs.features.games.di

import dagger.Subcomponent
import javax.inject.Qualifier

@PerGame
@Subcomponent(modules = [GameModule::class])
interface GameComponent {
    @Subcomponent.Builder
    interface Builder {
        fun viewModule(gameModule: GameModule): Builder
        fun build(): GameModule
    }
}


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PerGame


