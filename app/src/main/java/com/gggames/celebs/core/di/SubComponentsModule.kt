package com.gggames.celebs.core.di

import com.gggames.celebs.features.games.di.GameComponent
import com.gggames.celebs.presentation.di.ViewComponent
import dagger.Module

@Module(subcomponents = [ViewComponent::class, GameComponent::class])
class SubComponentsModule {}