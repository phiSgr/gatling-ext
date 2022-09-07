@file:Suppress("UNCHECKED_CAST")

package com.github.phisgr.gatling.kt.internal

import io.gatling.commons.validation.Validation
import io.gatling.javaapi.core.ActionBuilder
import io.gatling.javaapi.core.CheckBuilder
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.core.internal.Expressions
import scala.Function1
import scala.Function2
import java.util.function.BiPredicate
import java.util.function.Predicate
import io.gatling.core.check.Check as CheckS
import io.gatling.core.session.Session as SessionS

abstract class ActionCheckBuilder<
    Self : ActionCheckBuilder<Self, Res, WrappedRes, Check>,
    Res,
    WrappedRes,
    Check : CheckS<WrappedRes>,
    >(
    private val from: From<Res>,
) : ActionBuilder {
    protected abstract fun buildCheck(builder: CheckBuilder): Check
    protected abstract fun addChecks(checks: List<Check>): Self

    @JvmSynthetic
    fun check(vararg checks: From<Res>.() -> CheckBuilder): Self =
        addChecks(checks.map { buildCheck(it.invoke(from)) })

    fun check(vararg checks: CheckBuilder): Self =
        addChecks(checks.map { buildCheck(it) })

    fun checkIf(condition: String): ConditionWithoutRes<Self, Res, WrappedRes, Check> =
        ConditionWithoutRes(this as Self, Expressions.toBooleanExpression(condition))

    @JvmSynthetic
    inline fun checkIf(
        crossinline condition: (Session) -> Boolean,
    ): ConditionWithoutRes<Self, Res, WrappedRes, Check> =
        ConditionWithoutRes(this as Self) { session -> boolValidation { condition(Session(session)) } }

    fun checkIf(
        condition: Predicate<Session>,
    ): ConditionWithoutRes<Self, Res, WrappedRes, Check> =
        ConditionWithoutRes(this as Self, condition.toExpression())

    @JvmSynthetic
    inline fun checkIf(
        crossinline condition: (WrappedRes, Session) -> Boolean,
    ): ConditionWithRes<Self, Res, WrappedRes, Check> =
        ConditionWithRes(this as Self) { res, session ->
            boolValidation { condition(res, Session(session)) }
        }

    fun checkIf(
        condition: BiPredicate<WrappedRes, Session>,
    ): ConditionWithRes<Self, Res, WrappedRes, Check> =
        ConditionWithRes(this as Self, condition.toFunction2())

    class ConditionWithoutRes<
        Self : ActionCheckBuilder<Self, Res, WrappedRes, Check>,
        Res,
        WrappedRes,
        Check : CheckS<WrappedRes>,
        >(
        private val builder: Self,
        private val condition: Function1<SessionS, Validation<PrimitiveBool>>,
    ) {
        @JvmSynthetic
        fun then(vararg checks: From<Res>.() -> CheckBuilder): Self =
            then(*checks.map { it.invoke(builder.from) }.toTypedArray())

        fun then(vararg checks: CheckBuilder): Self =
            builder.addChecks(
                checks.map {
                    builder.buildCheck(it).checkIf(condition) as Check
                }
            )

    }

    class ConditionWithRes<
        Self : ActionCheckBuilder<Self, Res, WrappedRes, Check>,
        Res,
        WrappedRes,
        Check : CheckS<WrappedRes>,
        >(
        private val builder: Self,
        private val condition: Function2<WrappedRes, SessionS, Validation<PrimitiveBool>>,
    ) {
        @JvmSynthetic
        fun then(vararg checks: From<Res>.() -> CheckBuilder): Self =
            then(*checks.map { it.invoke(builder.from) }.toTypedArray())

        fun then(vararg checks: CheckBuilder): Self =
            builder.addChecks(
                checks.map {
                    builder.buildCheck(it).checkIf(condition) as Check
                }
            )
    }

}
