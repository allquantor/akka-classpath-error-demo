/*
 * Copyright (c) Allquantor 2017
 */

package io.allquantor

import akka.actor._

object Reaper {

  final val Name = "reaper"

  def props: Props =
    Props(new Reaper)
}

/**
  * Creates and watches important actors of classpath-error-showcase and shuts down the actor system under various conditions.
  */
class Reaper extends Actor with ActorLogging {

  context.watch(context.actorOf(HelloWorld.props, HelloWorld.Name))

  override def receive: Receive = {
    case Terminated(child) =>
      log.info("Terminating classpath-error-showcase: {} is terminated.", child.path)
      context.stop(self)
  }
}
