@file:JvmMultifileClass
@file:JvmName("ExtDsl")

package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.generic.SessionCombiner
import com.github.phisgr.gatling.generic.action.AsyncActionBuilder
import com.github.phisgr.gatling.generic.action.AwaitActionBuilder
import com.github.phisgr.gatling.kt.internal.ActionBuilderWrapper
import com.github.phisgr.gatling.kt.internal.elString
import io.gatling.javaapi.core.ActionBuilder

fun async(sessionAttributeNameEl: String, action: ActionBuilder): ActionBuilder =
    ActionBuilderWrapper(
        AsyncActionBuilder(sessionAttributeNameEl.elString, action.asScala())
    )

fun await(sessionAttributeNameEl: String, sessionCombiner: SessionCombiner): ActionBuilder =
    ActionBuilderWrapper(
        AwaitActionBuilder(sessionAttributeNameEl.elString, sessionCombiner)
    )
