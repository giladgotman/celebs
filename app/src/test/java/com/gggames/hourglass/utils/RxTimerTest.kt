package com.gggames.hourglass.utils

import com.google.common.truth.Truth.assertThat
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
    fun `Good weather flow - start`() {
        tested.time = 1000
        val observer = tested.observe().test()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().last()).isEqualTo(TimerEvent.TimerEnd)
    }


    @Test
    fun `Can do start stop start`() {
        tested.time = 2000
        val observer = tested.observe().test()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1000))

        tested.stop()

        assertThat(tested.time).isEqualTo(1000)
        observer.values().clear()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(0))
        assertThat(observer.values().last()).isEqualTo(TimerEvent.TimerEnd)
    }

    @Test
    fun `Good weather flow - pause resume`() {
        tested.time = 2000
        val observer = tested.observe().test()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1000))
        observer.values().clear()

        tested.pause()
        timerObservable.onNext(0)
        observer.assertNoValues()

        tested.resume()
        timerObservable.onNext(0)
        assertThat(observer.values().last()).isEqualTo(TimerEvent.TimerEnd)
    }
}