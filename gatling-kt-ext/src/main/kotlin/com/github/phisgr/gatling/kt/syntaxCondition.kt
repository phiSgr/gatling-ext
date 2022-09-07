package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.kt.internal.MutableBuilder
import com.github.phisgr.gatling.kt.internal.toChainBuilder
import io.gatling.javaapi.core.Choice
import io.gatling.javaapi.core.StructureBuilder
import io.gatling.javaapi.core.condition.*

fun <T : StructureBuilder<T, *>> DoIf.Then<T>.then(action: MutableBuilder.() -> Unit): T =
    then(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoIfEquals.Then<T>.then(action: MutableBuilder.() -> Unit): T =
    then(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoIfEqualsOrElse.Then<T>.then(action: MutableBuilder.() -> Unit): DoIfEqualsOrElse.OrElse<T> =
    then(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoIfEqualsOrElse.OrElse<T>.orElse(action: MutableBuilder.() -> Unit): T =
    orElse(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoIfOrElse.Then<T>.then(action: MutableBuilder.() -> Unit): DoIfOrElse.OrElse<T> =
    then(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoIfOrElse.OrElse<T>.orElse(action: MutableBuilder.() -> Unit): T =
    orElse(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> DoSwitchOrElse.OrElse<T>.orElse(action: MutableBuilder.() -> Unit): T =
    orElse(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> RandomSwitchOrElse.OrElse<T>.orElse(action: MutableBuilder.() -> Unit): T =
    orElse(action.toChainBuilder())

fun <T : StructureBuilder<T, *>> RoundRobinSwitch.On<T>.on(vararg action: MutableBuilder.() -> Unit): T =
    on(action.map { it.toChainBuilder() })

fun <T : StructureBuilder<T, *>> UniformRandomSwitch.On<T>.on(vararg action: MutableBuilder.() -> Unit): T =
    on(action.map { it.toChainBuilder() })

fun withKey(key: Any, action: MutableBuilder.() -> Unit): Choice.WithKey =
    Choice.withKey(key, action.toChainBuilder())

fun withWeight(weight: Double, action: MutableBuilder.() -> Unit): Choice.WithWeight =
    Choice.withWeight(weight, action.toChainBuilder())
