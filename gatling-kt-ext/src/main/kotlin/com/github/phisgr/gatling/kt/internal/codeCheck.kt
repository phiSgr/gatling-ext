package com.github.phisgr.gatling.kt.internal

import io.gatling.javaapi.core.CheckBuilder

object CodeCheckType : CheckBuilder.CheckType

val codeCheck = From<Nothing>("response", CodeCheckType)
