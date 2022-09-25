@file:JvmMultifileClass
@file:JvmName("ExtDsl")

package com.github.phisgr.gatling.kt

import com.github.phisgr.gatling.generic.Predef
import com.github.phisgr.gatling.generic.check.ResponseExtract
import com.github.phisgr.gatling.kt.internal.CodeCheckType
import io.gatling.javaapi.core.CheckBuilder

/**
 * Checks the [Throwable.message] of the potential failure in generic code execution.
 */
val errorMessage: CheckBuilder.Find<String?> =
    CheckBuilder.Find.Default(
        ResponseExtract.extract(Predef.errorMessage(), "response"),
        CodeCheckType,
        String::class.java,
        null
    )
