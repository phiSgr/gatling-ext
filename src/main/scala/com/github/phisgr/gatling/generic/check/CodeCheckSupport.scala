package com.github.phisgr.gatling.generic.check

import io.gatling.commons.util.Throwables.PimpedException
import io.gatling.commons.validation.{FailureWrapper, SuccessWrapper, Validation}
import io.gatling.core.check.{CheckBuilder, FindCheckBuilder, ValidatorCheckBuilder}

import scala.util.{Failure, Success, Try}

trait CodeCheckSupport {
  def errorMessage: Try[Any] => Validation[Option[String]] = {
    case Failure(exception) => Option(exception.getMessage).success
    case Success(_) => "The call did not fail.".failure
  }

  private val ReturnValue: Try[Any] => Validation[Option[Any]] = {
    case Failure(exception) => exception.detailedMessage.failure
    case Success(value) => Some(value).success
  }

  def returnValue[T]: Try[T] => Validation[Option[T]] =
    ReturnValue.asInstanceOf[Try[T] => Validation[Option[T]]]

  implicit def checkBuilder2CodeCheck[T, X](
    checkBuilder: CheckBuilder[ResponseExtract, Try[T], X]
  ): CodeCheck[T] =
    checkBuilder.build(CodeCheck.materializer)

  implicit def validatorCheckBuilder2CodeCheck[T, X](
    validatorCheckBuilder: ValidatorCheckBuilder[ResponseExtract, Try[T], X])
  : CodeCheck[T] =
    validatorCheckBuilder.exists

  implicit def findCheckBuilder2CodeCheck[T, X](
    findCheckBuilder: FindCheckBuilder[ResponseExtract, Try[T], X]
  ): CodeCheck[T] =
    findCheckBuilder.find.exists
}
