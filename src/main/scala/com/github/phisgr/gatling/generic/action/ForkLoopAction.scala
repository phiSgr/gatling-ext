package com.github.phisgr.gatling.generic.action

import java.util.concurrent.TimeUnit

import com.github.phisgr.gatling.generic.SessionCombiner
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.session.{Expression, ExpressionSuccessWrapper, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.{ChainBuilder, ScenarioContext}
import io.gatling.core.util.NameGen

import scala.concurrent.duration.FiniteDuration

class Looper(
  looperName: String,
  safePace: Expression[FiniteDuration],
  clock: Clock,
  loopBody: Action
) extends StrictLogging {
  private[this] var loopBranch: Session = _

  private[this] var stopped = false

  var prevRun: Long = Long.MinValue

  def stop(mainSession: Session, sessionCombiner: SessionCombiner, next: Action): Unit = {
    stopped = true
    next ! sessionCombiner.combineSafely(
      main = mainSession.remove(looperName),
      branched = loopBranch.remove(looperName),
      logger = logger
    )
  }

  def run(session: Session): Unit = {
    loopBranch = session
    if (!stopped) {
      safePace(session) match {
        case Success(duration) =>
          val next = duration.toMillis + prevRun
          val now = clock.nowMillis
          if (now >= next) {
            prevRun = now
            loopBody ! session
          } else {
            prevRun = next
            session.eventLoop.schedule({ () =>
              loopBody ! session
            }: Runnable, next - now, TimeUnit.MILLISECONDS)
          }
        case Failure(message) =>
          logger.warn(s"Scheduling of $looperName failed: $message")
          loopBranch = loopBranch.markAsFailed
      }
    } else {
      logger.info(s"Looper $looperName for UserId #${session.userId} stopped running.")
    }
  }
}

class ForkStopActionBuilder(looperName: String, sessionCombiner: SessionCombiner) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action =
    new ForkStopAction(looperName, sessionCombiner, ctx, next)
}

class ForkStopAction(
  looperName: String,
  sessionCombiner: SessionCombiner,
  ctx: ScenarioContext,
  override val next: Action
) extends RequestAction with NameGen {
  override val name: String = genName("stopLooper")

  override def requestName: Expression[String] = s"Stop $looperName".expressionSuccess

  override def sendRequest(requestName: String, session: Session): Validation[Unit] = {
    session(looperName).validate[Looper].map { looper =>
      looper.stop(session, sessionCombiner, next)
    }
  }

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine
  override def clock: Clock = ctx.coreComponents.clock
}

class Loop(looperName: String) extends Action with NameGen {
  override val name: String = genName("forkedLoop")
  override protected def execute(session: Session): Unit = {
    val looper = session(looperName).as[Looper]
    looper.run(session)
  }
}

class ForkLoopAction(looperName: String, pace: Expression[FiniteDuration], chain: ChainBuilder) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = new Action with NameGen {
    private val loopBody = chain.actionBuilders.foldLeft(new Loop(looperName): Action) { (next, actionBuilder) =>
      actionBuilder.build(ctx, next)
    }

    override val name: String = genName("startLooper")
    override protected def execute(session: Session): Unit = {
      val looper = new Looper(looperName, pace.safe, ctx.coreComponents.clock, loopBody)
      val withLooper = session.set(looperName, looper)
      try {
        looper.run(withLooper.copy(blockStack = Nil))
      } finally {
        next ! withLooper
      }
    }
  }
}
