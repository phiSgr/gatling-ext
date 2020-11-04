package com.github.phisgr

import java.util.{Timer, TimerTask}

package object example {
  val timer = new Timer
  def delay(time: Long)(task: => Unit): Unit = {
    timer.schedule(new TimerTask {
      override def run(): Unit = {
        task
      }
    }, time)
  }
}
