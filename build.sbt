// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `classpath-error-showcase` =
  project
    .in(file("."))
    .enablePlugins(
      AutomateHeaderPlugin,
      EcrPlugin,
      JavaAppPackaging,
      GitVersioning,
      GitBranchPrompt
    )
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(commonSettings)
    .settings(packageSettings)
    .settings(
      libraryDependencies ++= Seq(
        // compile time dependencies
        library.akkaActor,
        library.akkaHttp,
        library.akkaStream,
        library.log4jApi,
        library.log4jCore,
        library.scalaLogging,
        // test dependencies
        library.akkaHttpTestkit % "it,test",
        library.akkaTestkit     % "it,test",
        library.scalaCheck      % "it,test",
        library.scalaTest       % "it,test"
      )
    )

// *****************************************************************************
// Dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka         = "2.5.6"
      val akkaHttp     = "10.0.10"
      val log4j        = "2.9.1"
      val scalaCheck   = "1.13.5"
      val scalaFmt     = "1.2.0"
      val scalaLogging = "3.7.2"
      val scalaTest    = "3.0.3"
      val scapeGoat    = "1.3.3"
    }
    val akkaActor       = "com.typesafe.akka"          %% "akka-actor"        % Version.akka
    val akkaHttp        = "com.typesafe.akka"          %% "akka-http"         % Version.akkaHttp
    val akkaHttpTestkit = "com.typesafe.akka"          %% "akka-http-testkit" % Version.akkaHttp
    val akkaStream      = "com.typesafe.akka"          %% "akka-stream"       % Version.akka
    val akkaTestkit     = "com.typesafe.akka"          %% "akka-testkit"      % Version.akka
    val log4jApi        = "org.apache.logging.log4j"   % "log4j-api"          % Version.log4j
    val log4jCore       = "org.apache.logging.log4j"   % "log4j-core"         % Version.log4j
    val scalaCheck      = "org.scalacheck"             %% "scalacheck"        % Version.scalaCheck
    val scalaLogging    = "com.typesafe.scala-logging" %% "scala-logging"     % Version.scalaLogging
    val scalaTest       = "org.scalatest"              %% "scalatest"         % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings =
  compilerSettings ++
    gitSettings ++
    licenseSettings ++
    organizationSettings ++
    sbtSettings ++
    scalaFmtSettings ++
    scapegoatSettings

lazy val packageSettings =
  dockerSettings ++
    ecrSettings ++
    releaseSettings

lazy val compilerSettings =
  Seq(
    scalaVersion := "2.12.3",
    mappings.in(Compile, packageBin) +=
      baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector",
      "-Xfatal-warnings",
      "-Ywarn-unused-import",
      "-Yno-adapted-args",
      "-Ywarn-numeric-widen",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-unused-import"
    ),
    javacOptions ++= Seq(
      "-source",
      "1.8",
      "-target",
      "1.8"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value)
  )

lazy val dockerSettings =
  Seq(
    packageSummary := """"My component"""",
    packageDescription := """"My component description"""",
    maintainer := "MOIA My Component <mycomponent@allquantor.io>",
    dockerExposedPorts := Seq(8080)
  )

import com.amazonaws.regions.{Region, Regions}

lazy val ecrSettings =
  Seq(
    region := Region.getRegion(Regions.EU_CENTRAL_1),
    repositoryName := (packageName in Docker).value,
    localDockerImage := (packageName in Docker).value + ":" + (version in Docker).value,
    push in ecr := ((push in ecr) dependsOn (publishLocal in Docker, login in ecr)).value
  )

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val licenseSettings =
  Seq(
    headerLicense := Some(
      HeaderLicense.Custom(
        """|Copyright (c) Allquantor 2017
         |""".stripMargin
      )),
    headerMappings := headerMappings.value + (HeaderFileType.conf -> HeaderCommentStyle.HashLineComment)
  )

lazy val organizationSettings =
  Seq(
    organization := "io.allquantor"
  )

import ReleaseTransformations._

lazy val releaseSettings =
  Seq(
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommand("scapegoat"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val sbtSettings =
  Seq(
    cancelable in Global := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)
  )

lazy val scalaFmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtVersion := library.Version.scalaFmt
  )

lazy val scapegoatSettings =
  Seq(
    scapegoatVersion := library.Version.scapeGoat
  )
