@file:JvmMultifileClass
@file:JvmName("ExtDsl")

package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.generic.Predef
import com.github.phisgr.gatling.kt.generic.CodeActionBuilder
import com.github.phisgr.gatling.kt.internal.elString
import io.gatling.javaapi.core.Session
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import scala.runtime.BoxedUnit
import scala.util.Failure
import scala.util.Success
import scala.util.control.NonFatal

inline fun <T> blockingAction(
    requestNameEl: String,
    crossinline action: (Session) -> T,
): CodeActionBuilder<T> =
    CodeActionBuilder(
        Predef.blockingAction(requestNameEl.elString, { action(Session(it)) }, null)
    )

/**
 * Optional feature.
 * `compileOnly` was used in Gradle for the coroutines dependency.
 *
 * [java.lang.NoClassDefFoundError] will be thrown if
 * `kotlinx-coroutines-core` is not available in Gatling class path
 * and this method is used.
 * When that happens, the user will be lost in the aether, and the simulation won't end.
 */
inline fun <T> coroutineAction(
    requestNameEl: String,
    crossinline action: suspend (Session) -> T,
): CodeActionBuilder<T> =
    CodeActionBuilder(
        Predef.callbackAction(requestNameEl.elString) { session, callback ->
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val res = try {
                    action(Session(session))
                } catch (e: Throwable) {
                    if (NonFatal.apply(e)) {
                        callback.apply(Failure(e))
                        return@launch
                    } else {
                        throw e
                    }
                }
                callback.apply(Success(res))
            }
            BoxedUnit.UNIT
        }
    )


inline fun <T> callbackAction(
    requestNameEl: String,
    crossinline action: (Session, callback: (Result<T>) -> Unit) -> Unit,
): CodeActionBuilder<T> =
    CodeActionBuilder(
        Predef.callbackAction(requestNameEl.elString) { session, callback ->
            action(Session(session)) { res ->
                res.fold(
                    { callback.apply(Success(it)) },
                    { callback.apply(Failure(it)) },
                )
            }
            BoxedUnit.UNIT
        }
    )
