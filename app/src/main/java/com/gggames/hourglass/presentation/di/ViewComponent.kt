package com.gggames.hourglass.presentation.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.gggames.hourglass.core.di.getAppComponent
import com.gggames.hourglass.presentation.common.NameBadge
import com.gggames.hourglass.presentation.creategame.AddCardsFragment
import com.gggames.hourglass.presentation.creategame.ChooseTeamFragment
import com.gggames.hourglass.presentation.creategame.CreateGameFragment
import com.gggames.hourglass.presentation.creategame.GamesFragment
import com.gggames.hourglass.presentation.endgame.GameOverFragment
import com.gggames.hourglass.presentation.endturn.EndTurnDialogFragment
import com.gggames.hourglass.presentation.gameon.GameOnFragment
import com.gggames.hourglass.presentation.gameon.GameOnFragmentMVI
import com.gggames.hourglass.presentation.video.VideoPlayerFragment
import dagger.Subcomponent
import javax.inject.Qualifier

fun createViewComponent(context: Context): ViewComponent =
    getAppComponent(context)
        .viewComponent().viewModule(
            ViewModule(
                context
            )
        ).build()

fun createViewComponent(fragment: Fragment): ViewComponent =
    createViewComponent(fragment.requireActivity())

@Subcomponent(modules = [ViewModule::class])
interface ViewComponent {

    @Subcomponent.Builder
    interface Builder {
        fun viewModule(view: ViewModule): Builder
        fun build(): ViewComponent
    }

    @ViewContext
    fun context(): Context

    fun inject(fragment: GamesFragment)
    fun inject(fragment: AddCardsFragment)
    fun inject(fragment: ChooseTeamFragment)
    fun inject(fragment: CreateGameFragment)
    fun inject(fragment: GameOnFragment)
    fun inject(fragment: GameOnFragmentMVI)
    fun inject(fragment: GameOverFragment)
    fun inject(fragment: VideoPlayerFragment)
    fun inject(fragment: EndTurnDialogFragment)
    fun inject(layout: NameBadge)
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewContext
