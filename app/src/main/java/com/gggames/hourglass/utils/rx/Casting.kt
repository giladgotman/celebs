package com.gggames.hourglass.utils.rx

import io.reactivex.Observable

inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)