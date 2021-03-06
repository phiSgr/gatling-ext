package com.github.phisgr.gatling.generic.check

import io.gatling.commons.validation.{SuccessWrapper, Validation}
import io.gatling.core.check.Check.PreparedCache
import io.gatling.core.check.{Check, CheckMaterializer, CheckResult, DefaultFindCheckBuilder, Extractor, Preparer, identityPreparer}
import io.gatling.core.session.{ExpressionSuccessWrapper, Session}

import scala.util.Try

case class CodeCheck[T](wrapped: Check[Try[T]]) extends Check[Try[T]] {
  override def check(response: Try[T], session: Session, preparedCache: PreparedCache): Validation[CheckResult] =
    wrapped.check(response, session, preparedCache)
}

object CodeCheck {

  def materializer[Res]: CheckMaterializer[ResponseExtract, CodeCheck[Res], Try[Res], Try[Res]] =
    new CheckMaterializer[ResponseExtract, CodeCheck[Res], Try[Res], Try[Res]](
      specializer = CodeCheck(_)
    ) {
      override protected def preparer: Preparer[Try[Res], Try[Res]] = identityPreparer
    }

  private val Default: List[CodeCheck[Any]] = new DefaultFindCheckBuilder(
    displayActualValue = true,
    extractor = new Extractor[Try[Any], scala.util.Failure[Any]] {
      val name = "failure"
      override val arity = "find"

      override final def apply(prepared: Try[Any]): Validation[Option[scala.util.Failure[Any]]] = (prepared match {
        case f: scala.util.Failure[Any] => Some(f)
        case _ => None
      }).success
    }.expressionSuccess
  ).find.notExists.build(materializer) :: Nil

  def defaultCheck[T]: List[CodeCheck[T]] = Default.asInstanceOf[List[CodeCheck[T]]]
}
