package com.github.phisgr.gatling.generic

import java.util.concurrent.Executors

import com.github.phisgr.gatling.generic.action._
import com.github.phisgr.gatling.generic.check.CodeCheckSupport
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.Validation
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.structure.{ChainBuilder, StructureBuilder}

import scala.annotation.implicitAmbiguous
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Predef extends CodeCheckSupport with StrictLogging {

  private lazy val blockingEc: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  object direct extends ExecutionContext {
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = {
      logger.error("Callback running failed", cause)
    }
  }

  // Generic Code Execution

  def callbackAction[T](requestName: Expression[String])(f: (Session, Try[T] => Unit) => Unit): CodeActionBuilder[T] =
    CodeActionBuilder(requestName, f)

  def futureAction[T](requestName: Expression[String])(f: Session => Future[T]): CodeActionBuilder[T] =
    callbackAction[T](requestName) { (session, callback) =>
      // This is fine because callback does the dispatch anyway
      f(session).onComplete(callback)(direct)
    }

  def blockingAction[T: NotValidation](requestName: Expression[String])(f: Session => T): CodeActionBuilder[T] = {
    // Create the blocking thread pool only when we use a blockingAction
    blockingEc
    futureAction(requestName) { session =>
      Future(f(session))(blockingEc)
    }
  }


  // Async-await

  implicit class AsyncAwaitOps[B <: StructureBuilder[B]](val builder: B) extends AnyVal {
    def async(sessionAttributeName: Expression[String])(actionBuilder: ActionBuilder): B =
      builder.exec(new AsyncActionBuilder(sessionAttributeName, actionBuilder))

    def await(sessionAttributeName: Expression[String])(sessionCombiner: SessionCombiner): B =
      builder.exec(new AwaitActionBuilder(sessionAttributeName, sessionCombiner))
  }

  private def emptyChain: ChainBuilder = io.gatling.core.Predef.exec(Nil)

  def async(sessionAttributeName: Expression[String])(actionBuilder: ActionBuilder): ChainBuilder =
    emptyChain.async(sessionAttributeName)(actionBuilder)

  def await(sessionAttributeName: Expression[String])(sessionCombiner: SessionCombiner): ChainBuilder =
    emptyChain.await(sessionAttributeName)(sessionCombiner)


  // Polling

  def forkAndLoop(looperName: String, pace: Expression[FiniteDuration])(chain: ChainBuilder): ActionBuilder =
    new ForkLoopAction(looperName, pace, chain)

  def stopLooper(looperName: String)(sessionCombiner: SessionCombiner): ActionBuilder =
    new ForkStopActionBuilder(looperName, sessionCombiner)


  // workaround against value2Expression for blockingAction
  trait NotValidation[T]
  implicit def notValidation[T]: NotValidation[T] = new NotValidation[T] {}

  @implicitAmbiguous("""
When you call blockingAction, you need to pass a function taking a session.
If your code does not depend on the session, write
{ _ => ... }
""")
  implicit def validation1[T]: NotValidation[Validation[T]] = new NotValidation[Validation[T]] {}
  implicit def validation2[T]: NotValidation[Validation[T]] = new NotValidation[Validation[T]] {}
}
