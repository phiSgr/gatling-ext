package com.github.phisgr.gatling.kt.internal

import io.gatling.javaapi.core.ActionBuilder
import io.gatling.core.action.builder.ActionBuilder as ActionBuilderS

data class ActionBuilderWrapper(val a: ActionBuilderS) : ActionBuilder {
    override fun asScala(): ActionBuilderS = a
}
