/*
 * Copyright (c) Allquantor 2017
 */

package io.allquantor

import akka.actor._
import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {

  val ActorSystemShutdownTimeout = duration("classpath-error-showcase.actor-system-shutdown-timeout")

  private def duration(key: String): FiniteDuration =
    Duration(config.getDuration(key, MILLISECONDS), MILLISECONDS)
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)
}
