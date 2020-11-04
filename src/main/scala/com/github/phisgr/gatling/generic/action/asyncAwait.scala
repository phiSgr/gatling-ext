package com.github.phisgr.gatling.generic.action

import com.github.phisgr.gatling.generic.SessionCombiner
import com.typesafe.scalalogging.Logger
import io.gatling.commons.validation.Success
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

class AsyncActionBuilder(
  attributeNameExpression: Expression[String],
  inner: ActionBuilder
) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val innerAction = inner.build(ctx, new FutureFillingAction(attributeNameExpression))
    new AsyncAction(attributeNameExpression, innerAction, next)
  }
}

class FutureFillingAction(attributeNameExpression: Expression[String]) extends Action with NameGen {
  override val name: String = genName("futureFilling")
  override protected def execute(session: Session): Unit = {
    val Success(sessionAttributeName) = attributeNameExpression(session)
    val future = session(sessionAttributeName).as[SessionHolder]

    future.asyncCompleted(session.remove(sessionAttributeName), logger = logger)
  }
}

class AsyncAction(
  attributeNameExpression: Expression[String],
  innerAction: Action,
  override val next: Action
) extends ChainableAction with NameGen {
  override val name: String = genName("async")
  override protected def execute(session: Session): Unit = {
    val Success(sessionAttributeName) = attributeNameExpression(session)
    val withFuture = session.set(sessionAttributeName, new SessionHolder)
    try {
      innerAction ! withFuture.copy(
        // Prevent block exit
        blockStack = Nil
      )
    } finally {
      next ! withFuture
    }
  }
}

class AwaitActionBuilder(sessionAttributeName: Expression[String], sessionCombiner: SessionCombiner) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action =
    new AwaitAction(sessionAttributeName, sessionCombiner, next)
}

class AwaitAction(
  attributeNameExpression: Expression[String],
  sessionCombiner: SessionCombiner,
  override val next: Action
) extends ChainableAction with NameGen {

  override val name: String = genName("await")

  override protected def execute(session: Session): Unit = {
    val Success(sessionAttributeName) = attributeNameExpression(session)
    val future = session(sessionAttributeName).as[SessionHolder]

    future.await(
      mainSession = session.remove(sessionAttributeName),
      next = next,
      sessionCombiner = sessionCombiner,
      logger = logger
    )
  }
}

class SessionHolder {
  private[this] var session: Session = _
  private[this] var next: Action = _
  private[this] var sessionCombiner: SessionCombiner = _

  def asyncCompleted(branchedSession: Session, logger: Logger): Unit = {
    session match {
      case null => session = branchedSession
      case main => next ! sessionCombiner.combineSafely(main = main, branched = branchedSession, logger = logger)
    }
  }

  def await(mainSession: Session, next: Action, sessionCombiner: SessionCombiner, logger: Logger): Unit = {
    session match {
      case null =>
        this.session = mainSession
        this.next = next
        this.sessionCombiner = sessionCombiner
      case branched =>
        next ! sessionCombiner.combineSafely(main = mainSession, branched = branched, logger)
    }
  }
}
