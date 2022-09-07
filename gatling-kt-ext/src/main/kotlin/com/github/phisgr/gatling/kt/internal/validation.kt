@file:Suppress("UNCHECKED_CAST")

package com.github.phisgr.gatling.kt.internal

import io.gatling.commons.util.Throwables
import io.gatling.commons.validation.Failure
import io.gatling.commons.validation.Success
import io.gatling.commons.validation.Validation
import scala.Function1
import scala.Option
import scala.Some
import scala.collection.immutable.Seq
import scala.util.control.NonFatal

inline fun <T> safely(f: () -> Validation<T>) = try {
    f()
} catch (e: Throwable) {
    handleThrowable(e) as Validation<T>
}

/**
 * Inline version of `[io.gatling.javaapi.core.internal.Expressions.validation]`
 */
inline fun <T> validation(f: () -> T): Validation<T> = safely { Success.apply(f()) }

/**
 * Like [validation], but no allocation of a new [Success] object.
 */
inline fun boolValidation(f: () -> Boolean): Validation<PrimitiveBool> = safely {
    if (f()) Validation.TrueSuccess() else Validation.FalseSuccess()
}

inline fun <T> optionalValidation(f: () -> T?): Validation<Option<T>> = safely {
    val res = f()
    val wrapped = if (res == null) Validation.NoneSuccess() else Success(Some(res))
    wrapped as Validation<Option<T>>
}

/**
 * See [com.github.phisgr.gatling.kt.failWith].
 */
data class FailureThrowable(val f: Failure) : RuntimeException(f.message()) {
    // No stacktrace to make it lighter weight
    override fun fillInStackTrace(): Throwable = this
}

/**
 * See `safely` in the `package object` of `[io.gatling.commons.validation]`.
 */
fun handleThrowable(e: Throwable): Failure =
    if (e is FailureThrowable) {
        e.f
    } else if (NonFatal.apply(e)) {
        val message = Throwables.`PimpedException$`.`MODULE$`.`detailedMessage$extension`(e)
        Failure.apply(message)
    } else {
        throw e
    }

inline fun <Res, T> toScalaF(crossinline f: (Res) -> T): Function1<Res, Validation<T>> =
    Function1 { res: Res -> validation { f(res) } }

inline fun <Res, T> toScalaOptionF(crossinline f: (Res) -> T?): Function1<Res, Validation<Option<T>>> =
    Function1 { res: Res -> optionalValidation { f(res) } }

inline fun <Res, T> toScalaSeqOptionF(crossinline f: (Res) -> List<T>?): Function1<Res, Validation<Option<Seq<T>>>> =
    Function1 { res: Res -> optionalValidation { f(res)?.toSeq() } }
