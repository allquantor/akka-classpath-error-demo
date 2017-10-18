/*
 * Copyright (c) Allquantor 2017
 */

package io.allquantor

import akka.actor._

object HelloWorld {

  final val Name = "hello-world"

  def props: Props =
    Props(new HelloWorld)
}

/**
  * Starts a greeter, sends him a message and waits for a reply.
  * Once the reply has been received, it shuts down itself.
  */
class HelloWorld extends Actor with ActorLogging {

  log.info("Starting greeter..")
  val greeter = context.actorOf(Greeter.props, Greeter.Name)
  log.info("Send greet message..")
  greeter ! Greeter.Greet

  def receive = {
    case Greeter.Done =>
      log.info("Great message has been sent.")
  }
}
