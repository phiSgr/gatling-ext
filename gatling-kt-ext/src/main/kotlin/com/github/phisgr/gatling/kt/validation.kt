@file:JvmName("Validation")

package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.FailureThrowable
import io.gatling.commons.util.Throwables
import io.gatling.commons.validation.Failure
import io.gatling.commons.validation.Success
import io.gatling.commons.validation.Validation

/**
 * Throws a [FailureThrowable], of which `gatling-kt-ext` is aware.
 *
 * Gatling's official code just does `throw new RuntimeException`,
 * this makes the [`detailedMessage`][Throwables.PimpedException.detailedMessage] more noisy than necessary.
 *
 * For use in Java, which does not have a bottom type,
 * this returns a generic [T] instead of [Nothing],
 * which would be more idiomatic in Kotlin.
 */
fun <T> failWith(message: String): T = throw FailureThrowable(Failure.apply(message))

fun <T> Validation<T>.getOrThrow(): T = when (this) {
    is Failure -> throw FailureThrowable(this)
    else -> (this as Success<T>).value()
}
