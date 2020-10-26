package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.games.di.GameComponent
import com.gggames.hourglass.presentation.di.ViewComponent
import dagger.Module

@Module(subcomponents = [ViewComponent::class, GameComponent::class])
class SubComponentsModule
