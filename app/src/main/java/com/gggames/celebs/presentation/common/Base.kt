package com.gggames.celebs.presentation.common

import io.reactivex.Completable

interface MainActivityDelegate {

    /**
     * If you return true the back press will not be taken into account, otherwise the activity will act naturally
     * @return true if your processing has priority if not false
     */
    fun onBackPressed(): Boolean

    fun onLogout(): Completable = Completable.complete()
}
