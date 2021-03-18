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
            field = value
            if ((value % 1000L) == 0L) {
                _events.onNext(TimerEvent.UpdatedTime(value))
            }
        }

    fun start(elapsedTime: Long? = null) {
        Timber.w("ttt start, elapsedTime: $elapsedTime")
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
                _events.onNext(TimerEvent.TimerEnd) }


        timerObservable.subscribe({
            if (time % 1000L == 0L) {
                Timber.w("ttt tick, time: $time, timerObservable: $timerObservable")
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
        if (state == TimerState.NoInit) {
            start(time)
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
