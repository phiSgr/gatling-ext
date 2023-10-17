@file:JvmMultifileClass
@file:JvmName("ExtDsl")

package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.generic.action.AsyncActionBuilder
import com.github.phisgr.gatling.generic.action.AwaitActionBuilder
import com.github.phisgr.gatling.kt.generic.SessionCombiner
import com.github.phisgr.gatling.kt.internal.ActionBuilderWrapper
import com.github.phisgr.gatling.kt.internal.elString
import io.gatling.javaapi.core.ActionBuilder
import com.github.phisgr.gatling.generic.SessionCombiner as SessionCombinerS

fun async(sessionAttributeNameEl: String, action: ActionBuilder): ActionBuilder =
    ActionBuilderWrapper(
        AsyncActionBuilder(sessionAttributeNameEl.elString, action.asScala())
    )

@Deprecated("Use the overload for the Kotlin wrapper type.", level = DeprecationLevel.HIDDEN)
fun await(sessionAttributeNameEl: String, sessionCombiner: SessionCombinerS): ActionBuilder =
    ActionBuilderWrapper(
        AwaitActionBuilder(sessionAttributeNameEl.elString, sessionCombiner)
    )

fun await(sessionAttributeNameEl: String, sessionCombiner: SessionCombiner): ActionBuilder =
    ActionBuilderWrapper(
        AwaitActionBuilder(sessionAttributeNameEl.elString, sessionCombiner)
    )
