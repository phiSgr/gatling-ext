package com.github.phisgr.gatling.generic.check

import io.gatling.commons.validation.{Validation, safely}
import io.gatling.core.check._
import io.gatling.core.session.{Expression, ExpressionSuccessWrapper}

private[gatling] object ResponseExtract {

  abstract class ResponseExtractor[T, X](override val name: String) extends Extractor[T, X] {
    def extract(prepared: T): Validation[Option[X]]
    override final def apply(prepared: T): Validation[Option[X]] = safely()(extract(prepared))
  }

  class SingleExtractor[T, X](f: T => Validation[Option[X]], name: String) extends ResponseExtractor[T, X](name) {
    override def extract(prepared: T): Validation[Option[X]] = f(prepared)
    override val arity = "find"
  }

  def extract[T, X](
    f: T => Validation[Option[X]],
    name: String = "response"
  ): FindCheckBuilder[ResponseExtract, T, X] = new DefaultFindCheckBuilder(
    displayActualValue = true,
    extractor = new SingleExtractor[T, X](f, name).expressionSuccess
  )

  def extractMultiple[T, X](
    f: T => Validation[Option[Seq[X]]],
    name: String = "response"
  ): DefaultMultipleFindCheckBuilder[ResponseExtract, T, X] = new DefaultMultipleFindCheckBuilder[ResponseExtract, T, X](
    displayActualValue = true
  ) {
    override def findExtractor(occurrence: Int): Expression[ResponseExtractor[T, X]] =
      new ResponseExtractor[T, X](name) {
        override def extract(prepared: T): Validation[Option[X]] = f(prepared).map(_.flatMap(s =>
          if (s.isDefinedAt(occurrence)) Some(s(occurrence)) else None
        ))

        // Since the arity traits got fused into the CriterionExtractors
        // and our criteria are functions that do not look good in string
        // I have no choice but to write them manually
        override val arity: String = if (occurrence == 0) "find" else s"find($occurrence)"
      }.expressionSuccess

    override def findAllExtractor: Expression[ResponseExtractor[T, Seq[X]]] =
      new ResponseExtractor[T, Seq[X]](name) {
        override def extract(prepared: T): Validation[Option[Seq[X]]] = f(prepared)
        override val arity = "findAll"
      }.expressionSuccess

    override def countExtractor: Expression[ResponseExtractor[T, Int]] =
      new ResponseExtractor[T, Int](name) {
        override def extract(prepared: T): Validation[Option[Int]] = f(prepared).map(_.map(_.size))
        override val arity = "count"
      }.expressionSuccess
  }
}

// phantom type for implicit materializer resolution
trait ResponseExtract
