package com.github.phisgr.gatling.generic

import com.typesafe.scalalogging.Logger
import io.gatling.core.session.Session

import scala.annotation.varargs
import scala.util.control.NonFatal

trait SessionCombiner {
  /**
   * Combines the main session and the branched session
   *
   * @return a new session from both the branched and main flow
   */
  def reconcile(main: Session, branched: Session): Session

  final def combineSafely(main: Session, branched: Session, logger: Logger): Session = try {
    reconcile(main = main, branched = branched)
  } catch {
    case NonFatal(e) =>
      logger.warn("Session combining failed", e)
      main.markAsFailed
  }
}


object SessionCombiner {
  val NoOp: SessionCombiner = (main: Session, _: Session) => main

  @varargs
  def pick(attributes: String*): SessionCombiner = (main: Session, branched: Session) => {
    attributes.foldLeft(main) { case (acc, key) =>
      branched.attributes.get(key) match {
        case Some(value) => acc.set(key, value)
        case None => acc
      }
    }
  }
}
