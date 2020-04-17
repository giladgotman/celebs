package com.idagio.app.core.utils.rx.scheduler

import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class TrampolineSchedulerProvider : BaseSchedulerProvider {

    override fun computation(): Scheduler = Schedulers.trampoline()

    override fun io(): Scheduler = Schedulers.trampoline()

    override fun ui(): Scheduler = Schedulers.trampoline()

    override fun <T> applyDefault(): ObservableTransformer<T, T> =
        ObservableTransformer { it.subscribeOn(io()).observeOn(ui()) }
}
