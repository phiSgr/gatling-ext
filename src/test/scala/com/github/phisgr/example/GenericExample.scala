package com.github.phisgr.example

import com.github.phisgr.gatling.generic.Predef._
import com.github.phisgr.gatling.generic.SessionCombiner
import io.gatling.core.Predef._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success}

class GenericExample extends Simulation {

  val errorMessageAttribute = "errorMessage"

  val s = scenario("Generic")
    .exec(_.set("start", System.currentTimeMillis()))
    .repeat(10, counterName = "i") {
      async("sleeper${i}")(blockingAction("Sleep") { _ =>
        Thread.sleep(1000)
      })
    }
    .repeat(10, counterName = "i") {
      await("sleeper${i}")(SessionCombiner.NoOp)
    }
    .exec { session =>
      val now = System.currentTimeMillis()
      println(s"Now is $now. Done in ${now - session("start").as[Long]} ms.")
      session
    }
    .exec(futureAction[Unit]("Function Fail") { _ =>
      throw new IllegalStateException("A request is not made.") with NoStackTrace
    })
    .async("errorAction")(
      callbackAction[Unit]("Expected Fail") { (_, callback) =>
        delay(1000) {
          callback(Failure(new IllegalArgumentException("Fail!")))
        }
      }.check(errorMessage)(_ saveAs errorMessageAttribute)
    )
    .exec(
      callbackAction[String]("Unexpected Fail") { (_, callback) =>
        callback(Failure(new IllegalArgumentException("Fail!")))
      }
    )
    .doIfOrElse(_.userId % 2 == 1) {
      await("errorAction")(SessionCombiner.pick(errorMessageAttribute))
    } {
      await("errorAction") { (_, _) =>
        throw new IllegalArgumentException("I want moar code coverage!") with NoStackTrace
      }
    }
    .exec { session =>
      println(s"User ID is ${session.userId}. Error message is '${session(errorMessageAttribute).asOption[String]}'.")
      session
    }
    .exec(
      callbackAction[String]("Expected Failure, Succeeded") { (_, callback) =>
        callback(Success("silent"))
      }.silent.check(errorMessage)(_.notExists)
    )
    .exec(
      futureAction("Check") { _ => Future(List(1, 2, 3)) }
        .checkSeq(returnValue)(
          _.find(2) is 3,
          _.find(0),
          _.find(3).notExists,
          _.count is 3,
          _.findAll is List(1, 2, 3)
        )
    )

  setUp(
    s.inject(atOnceUsers(5))
  )
}
