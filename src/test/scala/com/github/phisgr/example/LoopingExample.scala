package com.github.phisgr.example

import com.github.phisgr.gatling.generic.Predef._
import com.github.phisgr.gatling.generic.SessionCombiner
import io.gatling.commons.validation.FailureWrapper
import io.gatling.core.Predef._
import io.gatling.core.session.Expression

import scala.concurrent.duration._
import scala.util.Try

class LoopingExample extends Simulation {

  val errorMessageAttribute = "errorMessage"
  val counter = "counter"
  val looperName = "looper"

  def countFrom(session: Session) = session(counter).asOption[Int].getOrElse(0)

  val loopPace: Expression[FiniteDuration] = _.attributes.get("outer") match {
    case Some(9) => "On the last time, I want the loop to fail.".failure
    case _ => 100.millis
  }

  val s = scenario("Fork Loop")
    .repeat(10, "outer") {
      exec(forkAndLoop(looperName, loopPace) {
        exec(blockingAction("Add One") { session =>
          Thread.sleep(10)
          countFrom(session) + 1
        }.check(returnValue)(_ saveAs counter))
      })
        .repeat(10, "inner") {
          exec(callbackAction[Int]("One") { (session, cb) =>
            delay(100) {
              cb(Try {
                if (session("outer").as[Int] == 8 && session("inner").as[Int] % 5 == 0) {
                  throw new RuntimeException
                }
                1
              })
            }
          }.check(returnValue)(
            _.transformWithSession { (count, session) =>
              countFrom(session) + count
            } saveAs counter
          ))
        }
        .exec(stopLooper(looperName) { (main, branched) =>
          main.set(counter, countFrom(branched) + countFrom(main))
        })
        .exec { session =>
          println(s"Count is ${countFrom(session)}.")
          session.remove(counter)
        }
    }
    .exec(stopLooper("nonExisting")(SessionCombiner.NoOp))

  setUp(
    s.inject(atOnceUsers(1))
  )
}
