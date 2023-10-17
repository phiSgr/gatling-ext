package com.github.phisgr.gatling.kt.generic

import io.gatling.javaapi.core.Session
import com.github.phisgr.gatling.generic.SessionCombiner as SessionCombinerS
import io.gatling.core.session.Session as SessionS

@FunctionalInterface
fun interface SessionCombiner : SessionCombinerS {
    companion object {
        @JvmField
        val NO_OP: SessionCombiner = SessionCombiner { main, _ -> main }

        @JvmStatic
        fun pick(vararg attributes: String): SessionCombiner = SessionCombiner { main, branched ->
            val unwrapped = main.asScala()
            val updated = attributes.fold(unwrapped) { acc, attributeName ->
                when (val value = branched.get<Any>(attributeName)) {
                    null -> acc
                    else -> acc.set(attributeName, value)
                }
            }
            Session(updated)
        }
    }

    override fun reconcile(main: SessionS, branched: SessionS): SessionS =
        reconcile(Session(main), Session(branched)).asScala()

    fun reconcile(main: Session, branched: Session): Session
}
