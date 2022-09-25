package com.github.phisgr.gatling.kt.internal

import io.gatling.javaapi.core.ActionBuilder
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl

fun (MutableBuilder.() -> Unit).toChainBuilder() = MutableBuilder().apply(this).toChainBuilder()

class MutableBuilder {
    private val builderAcc = mutableListOf<ChainBuilder>()

    fun toChainBuilder(): ChainBuilder {
        return CoreDsl.exec(builderAcc)
    }

    operator fun ActionBuilder.unaryPlus() {
        builderAcc += CoreDsl.exec(this)
    }

    operator fun ChainBuilder.unaryPlus() {
        builderAcc += this
    }

}
