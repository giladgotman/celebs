package com.gggames.celebs.di

import com.gggames.celebs.presentation.GamesFragment
import dagger.Subcomponent

@Subcomponent
interface ViewComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ViewComponent
    }

    fun inject(gamesFragment: GamesFragment)
}