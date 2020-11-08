package com.gggames.hourglass.presentation.onboarding

import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.presentation.onboarding.WelcomeContract.View
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class WelcomePresenter @Inject constructor(
    private val authenticator: Authenticator
) : WelcomeContract {

    lateinit var view: View

    private val subscriptions = CompositeDisposable()


    override fun bind(view: View) {
        this.view = view

        if (authenticator.me != null) {
            view.redirectToMain()
        }
    }

    override fun unBind() {
        subscriptions.dispose()
    }
}


interface WelcomeContract {
    fun bind(view: View)
    fun unBind()

    interface View {
        fun redirectToMain()
    }
}
