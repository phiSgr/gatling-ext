package com.github.phisgr.gatling.generic.action

import com.github.phisgr.gatling.generic.check.{CodeCheck, ResponseExtract}
import io.gatling.commons.validation.Validation
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.check.CheckBuilder
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.structure.ScenarioContext

import scala.util.Try

case class CodeActionBuilder[T](
  requestName: Expression[String],
  f: (Session, Try[T] => Unit) => Unit,
  private val checks: List[CodeCheck[T]] = Nil,
  isSilent: Boolean = false
) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val resolvedCheck = if (checks.isEmpty) CodeCheck.defaultCheck[T] else checks
    new CodeAction[T](
      requestName = requestName,
      f = f,
      checks = resolvedCheck,
      isSilent = isSilent,
      statsEngine = ctx.coreComponents.statsEngine,
      clock = ctx.coreComponents.clock,
      next = next
    )
  }

  def check[X](
    extract: Try[T] => Validation[Option[X]])(
    checks: (CheckBuilder.Find[ResponseExtract, Try[T], X] => CodeCheck[T])*
  ): CodeActionBuilder[T] = {
    val checkBuilder = ResponseExtract.extract(extract)
    copy(checks = this.checks ::: checks.map(_.apply(checkBuilder)).toList)
  }

  def checkSeq[X](
    extract: Try[T] => Validation[Option[Seq[X]]])(
    checks: (CheckBuilder.MultipleFind[ResponseExtract, Try[T], X] => CodeCheck[T])*
  ): CodeActionBuilder[T] = {
    val checkBuilder = ResponseExtract.extractMultiple(extract)
    copy(checks = this.checks ::: checks.map(_.apply(checkBuilder)).toList)
  }

  def silent: CodeActionBuilder[T] = copy(isSilent = true)
}
