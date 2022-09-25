package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.ActionBuilderWrapper
import com.github.phisgr.gatling.kt.internal.toScalaF
import io.gatling.core.Predef
import io.gatling.javaapi.core.ActionBuilder
import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.Session
import io.gatling.core.session.Session as SessionS
import io.gatling.core.structure.ChainBuilder as ChainBuilderS

typealias SessionHook = (Session) -> Session

/**
 * Alias for [CoreDsl.exec].
 *
 * For a Kotlin lambda without an explicit parameter,
 * the correct overload, that takes [java.util.function.Function],
 * cannot be resolved,
 *
 * because the other possibility, the [ActionBuilder],
 * is also a SAM type.
 *
 * This `inline` function emits a [scala.Function1], it may be more efficient.
 */
inline fun hook(crossinline f: SessionHook): ActionBuilder {
    val chain = Predef.exec(toScalaF { s: SessionS -> f(Session(s)).asScala() }) as ChainBuilderS
    require(chain.actionBuilders().sizeCompare(1) == 0) { "Unexpected chain length" }
    return ActionBuilderWrapper(chain.actionBuilders().head())
}

/**
 * Take a peek at the session.
 */
inline fun peek(crossinline f: (Session) -> Unit): ActionBuilder =
    hook {
        f(it)
        it
    }
