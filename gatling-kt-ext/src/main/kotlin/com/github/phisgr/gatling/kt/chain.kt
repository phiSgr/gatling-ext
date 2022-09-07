package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.MutableBuilder
import com.github.phisgr.gatling.kt.internal.toChainBuilder
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.ScenarioBuilder

/**
 * To create a ChainBuilder
 */
fun chain(action: MutableBuilder.() -> Unit): ChainBuilder =
    action.toChainBuilder()

fun scenario(name: String, action: MutableBuilder.() -> Unit): ScenarioBuilder =
    scenario(name).exec(action.toChainBuilder())
