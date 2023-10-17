package com.github.phisgr.gatling.kt.internal

import io.gatling.commons.validation.Validation
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.internal.Expressions
import scala.Function1
import scala.Function2
import scala.collection.immutable.ArraySeq
import scala.collection.immutable.Seq
import java.util.function.BiPredicate
import java.util.function.Predicate

import io.gatling.core.session.Session as SessionS

/**
 * Scala acts weird with primitive wrappers
 */
typealias PrimitiveBool = Any

val String.elString: Function1<SessionS, Validation<String>> get() = Expressions.toStringExpression(this)

@Suppress("UNCHECKED_CAST")
fun <T> List<T>.toSeq(): Seq<T> =
    ArraySeq.unsafeWrapArray<Any?>(this.toTypedArray<Any?>()) as Seq<T>

/**
 * Like [Expressions.javaBooleanFunctionToExpression], but for the specialized [Predicate] type.
 */
fun Predicate<Session>.toExpression(): Function1<SessionS, Validation<PrimitiveBool>> =
    Function1 { session -> boolValidation { this.test(Session(session)) } }

fun <T> BiPredicate<T, Session>.toFunction2(): Function2<T, SessionS, Validation<PrimitiveBool>> =
    Function2 { t, session -> boolValidation { this.test(t, Session(session)) } }
