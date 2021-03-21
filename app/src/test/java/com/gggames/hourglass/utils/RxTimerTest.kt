package com.gggames.hourglass.utils

import com.google.common.truth.Truth.assertThat
import com.idagio.app.core.utils.rx.scheduler.TrampolineSchedulerProvider
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class RxTimerTest{
    private val tested = RxTimer(TrampolineSchedulerProvider())
    private val timerObservable = PublishSubject.create<Long>()

    @Before
    fun setup() {
        tested.createTimerObservable = {
            timerObservable
        }
    }
    @Test
    fun `Good weather flow - start`() {
        tested.time = 100
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
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1900))

        tested.stop()

        assertThat(tested.time).isEqualTo(1900)
        observer.values().clear()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1800))
    }

    @Test
    fun `Good weather flow - pause resume`() {
        tested.time = 2000
        val observer = tested.observe().test()
        tested.start()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1900))
        observer.values().clear()

        tested.pause()
        timerObservable.onNext(0)
        observer.assertNoValues()

        tested.resume()
        timerObservable.onNext(0)
        assertThat(observer.values().first()).isEqualTo(TimerEvent.UpdatedTime(1800))
    }

    @Test
    fun `update time rounds it`() {
        tested.updateTime(1200)
        assertThat(tested.time).isEqualTo(1000)

        tested.updateTime(1000)
        assertThat(tested.time).isEqualTo(1000)

        tested.updateTime(0)
        assertThat(tested.time).isEqualTo(0)

        tested.updateTime(999)
        assertThat(tested.time).isEqualTo(0)

        tested.updateTime(60000)
        assertThat(tested.time).isEqualTo(60000)

        tested.updateTime(59999)
        assertThat(tested.time).isEqualTo(59000)
    }
}