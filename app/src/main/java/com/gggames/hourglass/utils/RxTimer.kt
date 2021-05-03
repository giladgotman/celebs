package com.gggames.hourglass.utils

import androidx.annotation.VisibleForTesting
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RxTimer constructor(val schedulerProvider: BaseSchedulerProvider) {
    private val disposables = CompositeDisposable()
    private val _events = PublishSubject.create<TimerEvent>()
    private val interval = 100L
    private var state = TimerState.NoInit

    @VisibleForTesting
    var createTimerObservable: () -> Observable<Long> = {
        Observable.interval(
            interval,
            interval,
            TimeUnit.MILLISECONDS,
            schedulerProvider.io()
        )
    }
    var time: Long = 0
        set(value) {
            field = if(value <0) 0 else value
            if ((value % 1000L) == 0L) {
                _events.onNext(TimerEvent.UpdatedTime(field))
            }
        }

    fun updateTime(time: Long) {
        val roundedTime = (time / 1000) * 1000
        Timber.v("update time: $time, rounded: $roundedTime")
        this.time = roundedTime
    }

    fun start(elapsedTime: Long? = null) {
        Timber.w("ttt start, elapsedTime: $elapsedTime, state: $state")

        if (state == TimerState.Running) {
            disposables.clear()
        }
        elapsedTime?.let {
            this.time = it
        }
        state = TimerState.Running

        val timerObservable = createTimerObservable()
            .filter { state == TimerState.Running }
            .map { time -= interval }
            .takeUntil { time <= 200 }
            .doOnComplete {
                time = 0
                _events.onNext(TimerEvent.TimerEnd)
            }

        timerObservable.subscribe({
            if (time <= 10000L && time % 1000L == 0L && time != 0L) {
                _events.onNext(TimerEvent.Tick(time))
            }
        }, {
            Timber.e(it, "Error while using RxTimer")
        })
            .let { disposables.add(it) }
    }

    fun stop() {
        state = TimerState.Stopped
        disposables.clear()
    }

    fun pause() {
        state = TimerState.Paused
    }

    fun resume() {
        Timber.w("ttt resume, state: $state, this: $this")
        if (state == TimerState.NoInit) {
            Timber.w("ttt resume, timer must be initted before resuming")
            return
        } else {
            state = TimerState.Running
        }
    }

    fun observe() = _events
}

enum class TimerState {
    NoInit,
    Running,
    Paused,
    Stopped
}

sealed class TimerEvent {
    object TimerEnd : TimerEvent()
    data class Tick(val time: Long) : TimerEvent()
    data class UpdatedTime(val time: Long) : TimerEvent()
}
