package com.github.phisgr.gatling.generic

import io.netty.channel.EventLoop

package object util {
  implicit class EventLoopHelper(val eventLoop: EventLoop) extends AnyVal {
    def checkAndExecute(command: Runnable): Unit = {
      if (!eventLoop.isShutdown) eventLoop.execute(command)
    }
  }
}
