package com.github.phisgr.gatling.generic.action

import com.github.phisgr.gatling.generic.check.CodeCheck
import com.github.phisgr.gatling.generic.util.EventLoopHelper
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.util.StringHelper.Eol
import io.gatling.commons.validation.Validation
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import io.gatling.netty.util.StringBuilderPool

import scala.util.Try

class CodeAction[T](
  override val requestName: Expression[String],
  f: (Session, Try[T] => Unit) => Unit,
  checks: List[CodeCheck[T]],
  isSilent: Boolean,
  override val statsEngine: StatsEngine,
  override val clock: Clock,
  override val next: Action
) extends RequestAction with NameGen {

  override val name: String = genName("code")

  override def sendRequest(session: Session): Validation[Unit] = {
    requestName(session).map { requestName =>
      val startTimestamp = clock.nowMillis

      f(session, { res =>
        val endTimestamp = clock.nowMillis
        session.eventLoop.checkAndExecute { () =>
          val (checkSaveUpdated, checkError) = Check.check(res, session, checks, preparedCache = null)

          val status = if (checkError.isEmpty) OK else KO
          val errorMessage = checkError.map(_.message)

          val newSession = if (isSilent) checkSaveUpdated else {
            val withStatus = if (status == KO) checkSaveUpdated.markAsFailed else checkSaveUpdated
            statsEngine.logResponse(
              withStatus.scenario,
              withStatus.groups,
              requestName,
              startTimestamp = startTimestamp,
              endTimestamp = endTimestamp,
              status = status,
              responseCode = None,
              message = errorMessage
            )
            withStatus.logGroupRequestTimings(startTimestamp = startTimestamp, endTimestamp = endTimestamp)
          }


          def dump = {
            StringBuilderPool.DEFAULT
              .get()
              .append(Eol)
              .append(">>>>>>>>>>>>>>>>>>>>>>>>>>").append(Eol)
              .append("Request:").append(Eol)
              .append(s"$requestName: $status ${errorMessage.getOrElse("")}").append(Eol)
              .append("=========================").append(Eol)
              .append(session).append(Eol)
              .append("=========================").append(Eol)
              .append("Code return:").append(Eol)
              .append(res).append(Eol)
              .append("<<<<<<<<<<<<<<<<<<<<<<<<<")
              .toString
          }

          if (status == KO) {
            logger.debug(s"Request '$requestName' failed for user ${session.userId}: ${errorMessage.getOrElse("")}")
            if (!logger.underlying.isTraceEnabled) {
              logger.debug(dump)
            }
          }
          logger.trace(dump)

          next ! newSession
        }
      })
    }
  }
}
