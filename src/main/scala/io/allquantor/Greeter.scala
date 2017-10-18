/*
 * Copyright (c) Allquantor 2017
 */

package io.allquantor

import akka.actor._

object Greeter {

  final val Name = "greeter"

  def props: Props =
    Props(new Greeter)

  case object Greet
  case object Done
}

/**
  * Whenever it receives a [[Greet]] message it replies with a [[Done]] message.
  */
class Greeter extends Actor with ActorLogging {

  import Greeter._

  def receive = {
    case Greet =>
      log.info("Hello World!")
      sender() ! Done
  }
}
