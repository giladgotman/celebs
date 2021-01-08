package com.gggames.hourglass.utils

import io.reactivex.observers.TestObserver
import java.util.concurrent.TimeUnit

fun <T> TestObserver<T>.waitForAllEvents(seconds: Long = 2L): TestObserver<T> =
    this.also { it.awaitTerminalEvent(seconds, TimeUnit.SECONDS) }

fun <T> TestObserver<T>.withValueAtPosition(index: Int, predicate: (value: T) -> Unit): TestObserver<T> =
    this.assertValueAt(index) {
        predicate(it)
        true
    }

fun <T> TestObserver<T>.withFirstValue(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(0) {
        predicate(it)
        true
    }
}

fun <T> TestObserver<T>.withSecondValue(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(1) {
        predicate(it)
        true
    }
}

fun <T> TestObserver<T>.withThirdValue(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(2) {
        predicate(it)
        true
    }
}

fun <T> TestObserver<T>.withFourthValue(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(3) {
        predicate(it)
        true
    }
}

fun <T> TestObserver<T>.withLastValue(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(valueCount() - 1) {
        predicate(it)
        true
    }
}

fun <T> TestObserver<T>.withValueBeforeLast(predicate: (T) -> Unit): TestObserver<T> {
    return this.assertValueAt(valueCount() - 2) {
        predicate(it)
        true
    }
}
