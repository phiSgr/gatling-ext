package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.MutableBuilder
import com.github.phisgr.gatling.kt.internal.toChainBuilder
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.StructureBuilder
import io.gatling.javaapi.core.error.Errors

fun <T : StructureBuilder<T, *>> Errors.TryMax<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun exitBlockOnFail(action: MutableBuilder.() -> Unit): ChainBuilder =
    CoreDsl.exitBlockOnFail(action.toChainBuilder())
