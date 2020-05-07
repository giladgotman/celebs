package com.gggames.celebs.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.gggames.celebs.presentation.GamesFragment
import dagger.Subcomponent
import javax.inject.Qualifier


fun createViewComponent(context: Context): ViewComponent =
    getAppComponent(context)
        .viewComponent().viewModule(ViewModule(context)).build()

fun createViewComponent(fragment: Fragment): ViewComponent =
    createViewComponent(fragment.requireActivity())


@Subcomponent (modules = [ViewModule::class])
interface ViewComponent {

    @Subcomponent.Builder
    interface Builder {
        fun viewModule(view: ViewModule): Builder
        fun build(): ViewComponent
    }

    @ViewContext
    fun context(): Context

    fun inject(gamesFragment: GamesFragment)
}


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewContext