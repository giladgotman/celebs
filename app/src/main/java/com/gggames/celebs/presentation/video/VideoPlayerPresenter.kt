package com.gggames.celebs.presentation.video

import com.gggames.celebs.features.video.PlayerEvent
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class VideoPlayerPresenter @Inject constructor(
    private val scheduler: BaseSchedulerProvider
){
    val disposabls = CompositeDisposable()

    fun bind(events: Observable<PlayerEvent>) {
        events.compose(scheduler.applyDefault())
            .subscribe({
                when (it) {
                    is PlayerEvent.OnIdleState -> {}
                    is PlayerEvent.OnReadyState -> {}
                    is PlayerEvent.OnBufferingState -> {}
                    is PlayerEvent.OnEndedState -> {}
                    is PlayerEvent.OnError -> {}
                }
            } , {
                Timber.e(it, "exception in observe PlayerEvent")
            }).let { disposabls.add(it)  }

    }
}
