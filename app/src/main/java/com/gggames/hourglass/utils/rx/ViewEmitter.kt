package com.gggames.hourglass.utils.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

interface EventEmitter<E> {
    fun events(): Observable<E>
    fun E.emit()
}

class ViewEventEmitter<E> : EventEmitter<E> {
    private val _events: Subject<E> = PublishSubject.create()
    override fun events(): Observable<E> = _events

    override fun E.emit() = _events.onNext(this)
}
