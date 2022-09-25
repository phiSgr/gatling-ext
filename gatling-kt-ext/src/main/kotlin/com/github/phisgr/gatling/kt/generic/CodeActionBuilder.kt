package com.github.phisgr.gatling.kt.generic

import com.github.phisgr.gatling.generic.Predef
import com.github.phisgr.gatling.generic.check.CodeCheck
import com.github.phisgr.gatling.generic.check.ResponseExtract
import com.github.phisgr.gatling.kt.internal.ActionCheckBuilder
import com.github.phisgr.gatling.kt.internal.codeCheck
import io.gatling.javaapi.core.CheckBuilder
import io.gatling.javaapi.core.internal.Converters
import scala.util.Try
import com.github.phisgr.gatling.generic.action.CodeActionBuilder as CodeActionBuilderS
import io.gatling.core.check.CheckBuilder as CheckBuilderS

class CodeActionBuilder<Res>(
    private val wrapped: CodeActionBuilderS<Res>,
    private val checks: List<CodeCheck<Res>> = emptyList(),
) : ActionCheckBuilder<
    CodeActionBuilder<Res>,
    Try<Res>,
    Try<Res>,
    CodeCheck<Res>
    >(codeCheck) {
    fun silent(): CodeActionBuilder<Res> = CodeActionBuilder(wrapped.silent(), checks)

    override fun asScala(): CodeActionBuilderS<Res> = wrapped
        .copy(
            wrapped.requestName(),
            wrapped.f(),
            Converters.toScalaSeq(checks).toList(),
            wrapped.isSilent
        )

    @Suppress("UNCHECKED_CAST")
    override fun buildCheck(builder: CheckBuilder): CodeCheck<Res> =
        Predef.checkBuilder2CodeCheck(
            builder.asScala() as CheckBuilderS<ResponseExtract, Try<Res>>
        )

    override fun addChecks(checks: List<CodeCheck<Res>>): CodeActionBuilder<Res> =
        CodeActionBuilder(wrapped, this.checks + checks)
}
