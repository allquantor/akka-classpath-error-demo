package io.allquantor

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, Terminated }
import akka.testkit.TestKitExtension
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest._

import akka.http.scaladsl.testkit.RouteTestTimeout

import scala.concurrent.duration._
import scala.concurrent.Await

object AkkaUnitTest {

  def config(defaultConfig: Config = ConfigFactory.load()): Config = {
    val extra =
      ConfigFactory.parseString("""|akka {
                                   |  actor {
                                   |    debug.fsm = off
                                   |  }
                                   |
                                   |  loggers = ["akka.testkit.TestEventListener"]
                                   |
                                   |  loglevel = debug
                                   |
                                   |  log-dead-letters                 = on
                                   |  log-dead-letters-during-shutdown = on
                                   |
                                   |  jvm-exit-on-fatal-error = off
                                   |
                                   |  test.timefactor = 1
                                   |
                                   |  diagnostics {
                                   |    recorder.enabled = off
                                   |    checker.enabled  = off
                                   |  }
                                   |}
                                   |""".stripMargin)
    ConfigFactory.defaultOverrides().withFallback(extra).withFallback(defaultConfig)
  }

  /**
    * Create an actor forwarding all of its child messages on to a parent. This is the proxy pattern as described
    * here: http://doc.akka.io/docs/akka/snapshot/scala/testing.html#Using_a_fabricated_parent.
    */
  def actorOfWithParent(system: ActorSystem, childProps: Props, name: String, parent: ActorRef): ActorRef =
    system.actorOf(Props(new Actor {
      val child = context.actorOf(childProps, name)
      context.watch(child)
      def receive = {
        case x: Terminated if sender == child => context.stop(self)
        case x if sender == child             => parent forward x
        case x                                => child forward x
      }
    }))
}

/**
  * Akka unit tests for classpath-error-showcase.
  */
trait AkkaUnitTestLike {

  protected def name: String

  protected def config(testData: Option[TestData]): Config =
    AkkaUnitTest.config()

  protected def startSystem(testData: Option[TestData]): ActorSystem =
    ActorSystem(s"classpath-error-showcase-spec", config(testData))

  protected def testTimeout(system: ActorSystem): Timeout =
    TestKitExtension(system).DefaultTimeout

  protected implicit def routeTestTimeout: RouteTestTimeout =
    RouteTestTimeout(2 seconds)

  protected def shutdownSystem(system: ActorSystem): Unit = {
    val whenTerminated = system.terminate()
    Await.result(whenTerminated, Duration.Inf)
  }
}

/**
  * Akka unit tests for classpath-error-showcase. The actor system along with
  * its configuration is established just once. Correspondingly the
  * actor system is terminated at the end of all of the tests.
  *
  * Note then that the startSystem and config methods (for example)
  * are passed no test data - there is no test data representing the
  * suite as a whole.
  */
abstract class AkkaUnitTest(override val name: String = "default")
  extends UnitTestLike
    with AkkaUnitTestLike
    with BeforeAndAfterAll {

  protected implicit val system =
    startSystem(None)

  protected implicit val timeout: Timeout = {
    val ext = TestKitExtension(system)
    Timeout(ext.DefaultTimeout.duration * ext.TestTimeFactor.toLong)
  }

  override protected def afterAll(): Unit =
    shutdownSystem(system)
}

/**
  * An test for Akka providing a new Akka system for each test run.
  *
  * Note then that the startSystem and config methods (for example)
  * are passed test data for each test that is executed. The test
  * therefore has the opportunity of overriding configuration for
  * each test (for example).
  */
abstract class AkkaUnitTestWithFixture(override val name: String = "default")
  extends fixture.WordSpec
    with Matchers
    with AkkaUnitTestLike {

  case class FixtureParam(system: ActorSystem, timeout: Timeout)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val system = startSystem(Some(test))
    try {
      val timeout = testTimeout(system)

      super.withFixture(test.toNoArgTest(FixtureParam(system, timeout)))
    } finally
      shutdownSystem(system)
  }
}
