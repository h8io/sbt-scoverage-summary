dynverSonatypeSnapshots := true
dynverSeparator := "-"

val plugin = projectMatrix.in(file("plugin"))
  .jvmPlatform(scalaVersions = Seq("3.8.4", "2.12.21"))
  .enablePlugins(SbtPlugin, ScoverageSummaryPlugin)
  .settings(
    name := "sbt-scoverage-summary",
    organization := "io.h8.sbt",
    organizationName := "H8IO",
    organizationHomepage := Some(url("https://github.com/h8io/")),
    description := "SBT scoverage summary",
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/h8io/sbt-scoverage-summary")),
    versionScheme := Some("semver-spec"),
    javacOptions ++= Seq("--release", "11"),
    developers := List(
      Developer(
        id = "eshu",
        name = "Pavel",
        email = "tjano.xibalba@gmail.com",
        url = url("https://github.com/h8io/")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/h8io/sbt-scoverage-summary"),
        "scm:git@github.com:h8io/sbt-scoverage-summary.git"
      )
    ),
    sbtPluginPublishLegacyMavenStyle := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.8.0"
        case _ => "2.0.0"
      }
    },
    scalacOptions ++=
      (scalaBinaryVersion.value match {
        case "2.12" =>
          Seq("-deprecation", "-feature", "-unchecked", "-Xfatal-warnings", "-Xlint:_", "-Ywarn-unused",
            "-Ywarn-dead-code", "-Ywarn-unused:-nowarn", "-Xsource:3")
        case _ => Seq("-deprecation", "-feature", "-unchecked", "-Werror", "-Wunused:all", "-Wvalue-discard")
      }),
    libraryDependencies ++= Seq(
      "org.scoverage" %% "scalac-scoverage-serializer" % "2.5.2",
      Defaults.sbtPluginExtra(
        "org.scoverage" % "sbt-scoverage" % "2.4.4",
        (pluginCrossBuild / sbtBinaryVersion).value,
        scalaBinaryVersion.value
      ),
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "org.scalamock" %% "scalamock" % "7.5.5" % Test
    ),
    libraryDependencies ++=
      (scalaBinaryVersion.value match {
        case "3" => Seq("org.scala-lang.modules" %% "scala-xml" % "2.4.0")
        case _ => Nil
      })
  )
