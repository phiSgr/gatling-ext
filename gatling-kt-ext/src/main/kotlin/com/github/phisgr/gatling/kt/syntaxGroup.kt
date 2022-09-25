package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.MutableBuilder
import com.github.phisgr.gatling.kt.internal.toChainBuilder
import io.gatling.javaapi.core.StructureBuilder
import io.gatling.javaapi.core.group.Groups

fun <T : StructureBuilder<T, *>> Groups.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())
