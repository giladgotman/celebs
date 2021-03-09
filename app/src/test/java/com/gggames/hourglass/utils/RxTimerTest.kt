package com.gggames.hourglass.utils

import com.google.common.truth.Truth
import com.idagio.app.core.utils.rx.scheduler.TrampolineSchedulerProvider
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class RxTimerTest{
    val tested = RxTimer(TrampolineSchedulerProvider())
    val timerObservable = PublishSubject.create<Long>()

    @Before
    fun setup() {
        tested.createTimerObservable = {
            timerObservable
        }
    }
    @Test
    fun `Good weather flow`() {
        tested.time = 1000
        val observer = tested.observe().test()
        tested.start()
        timerObservable.onNext(0)
        Truth.assertThat(observer.values().last()).isEqualTo(TimerEvent.TimerEnd)
    }
}