/*
 * Copyright (c) Allquantor 2017
 */

package io.allquantor

import java.util.concurrent.TimeoutException
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigException
import akka.actor._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

/**
  * The main entry point into the classpath-error-showcase.
  */
object Main {

  private val DefaultShutdownTimeout = 60.seconds

  @SuppressWarnings(Array("UnusedMethodParameter"))
  def main(args: Array[String]): Unit = {
    val system = startup()

    // Do not call `System.exit()` as part of shutdown hook thread, since calling `System.exit()` in shutdown thread
    // will lead to the method call to block indefinitely:
    // https://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#exit-int-
    sys.addShutdownHook(shutdown(system))
  }

  private def startup(): ActorSystem = {

    println("classpath-error-showcase is starting up..\n")

    implicit val system = ActorSystem("classpath-error-showcase")
    implicit val mat    = ActorMaterializer.create(system)

    val route: Route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hi!</h1>"))
        }
      }

    val server = Http().bindAndHandle(route, "0.0.0.0", 5000)

    try {
      system.actorOf(Reaper.props, Reaper.Name)
      system
    } catch {
      case NonFatal(e) =>
        system.log.error("Unable to start classpath-error-showcase: {}", e.getMessage)
        shutdown(system)
        sys.exit(1)
    }
  }

  private def shutdown(system: ActorSystem): Unit = {
    system.log.info("Preparing for shutdown..")

    val shutdownTimeout =
      try {
        Settings(system).ActorSystemShutdownTimeout
      } catch {
        case e: ConfigException =>
          system.log.warning(
            "Failure obtaining shutdown timeout configuration [{}] - falling back to default timeout [{}]",
            e.getMessage,
            DefaultShutdownTimeout
          )
          DefaultShutdownTimeout
      }

    system.log.info("Shutting down actor system..")

    try {
      Await.ready(system.terminate(), shutdownTimeout)
    } catch {
      case _: TimeoutException =>
        println("ERROR - Timeout waiting for actor system shutdown. Exiting as we cannot continue.")
        System.exit(1)
    }
  }
}
