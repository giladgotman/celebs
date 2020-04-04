package com.idagio.app.core.utils.rx.scheduler

import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.SingleTransformer

interface BaseSchedulerProvider {

    fun computation(): Scheduler

    fun io(): Scheduler

    fun ui(): Scheduler

    fun <T> applyDefaultSchedulers(): ObservableTransformer<T, T> =
        ObservableTransformer { it.subscribeOn(io()).observeOn(ui()) }

    fun <T> applySingleDefaultSchedulers(): SingleTransformer<T, T> =
        SingleTransformer { it.subscribeOn(io()).observeOn(ui()) }

    fun applyDefault(): CompletableTransformer =
        CompletableTransformer { it.subscribeOn(io()).observeOn(ui()) }
}
