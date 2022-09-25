package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.MutableBuilder
import com.github.phisgr.gatling.kt.internal.toChainBuilder
import io.gatling.javaapi.core.StructureBuilder
import io.gatling.javaapi.core.loop.*

fun <T : StructureBuilder<T, *>> AsLongAs.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> AsLongAsDuring.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoWhile.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoWhileDuring.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> During.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> ForEach.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> Forever.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> Repeat.On<T>.on(action: MutableBuilder.() -> Unit): T =
    on(action.toChainBuilder())
