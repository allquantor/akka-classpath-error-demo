package io.allquantor

import akka.actor._
import org.scalatest.TestData
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.Future
import akka.testkit.TestProbe

object ReaperSpec {

  val childActors =
    Seq(
      HelloWorld.Name
    )

  val packageName = "classpath-error-showcase"
  val testConfig =
    ConfigFactory.parseString(
      s"""|$packageName {
          |  actor-system-shutdown-timeout = 5 seconds
          |}
          |""".stripMargin
    )
}

class ReaperSpec extends AkkaUnitTest with ScalaFutures with Eventually with IntegrationPatience {

  import ReaperSpec._

  "The reaper" should {
    "start up" in {
      val reaper = system.actorOf(Reaper.props, "reaper-test")

      def findChildActor(actorName: String): Future[ActorRef] =
        system.actorSelection(s"${reaper.path}/$actorName").resolveOne()(timeout)

      childActors.foreach { childActorName =>
        eventually(whenReady(findChildActor(childActorName))(identity[ActorRef]))
      }
    }

    "shutdown when child actor is terminated" in {
      val reaper = system.actorOf(Reaper.props, Reaper.Name)

      val monitor = TestProbe()
      monitor.watch(reaper)

      def findChildActor(actorName: String): Future[ActorRef] =
        system.actorSelection(s"${reaper.path}/$actorName").resolveOne()(timeout)

      val childActor = eventually(whenReady(findChildActor(s"${childActors.head}"))(identity[ActorRef]))
      system.stop(childActor)

      monitor.expectTerminated(reaper)
    }
  }

  override protected def config(testData: Option[TestData]): Config =
    ConfigFactory.defaultOverrides().withFallback(super.config(testData))
}
