ThisBuild / organization := "io.h8.sbt"
ThisBuild / organizationName := "H8IO"
ThisBuild / organizationHomepage := Some(url("https://github.com/h8io/"))
ThisBuild / homepage := Some(url("https://github.com/h8io/sbt-scoverage-summary"))

ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalaVersion := "2.12.21"
ThisBuild / crossScalaVersions := Seq("2.12.21", "3.8.4")

ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
ThisBuild / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 12)) =>
    Seq("-Xfatal-warnings", "-Xlint:_", "-Ywarn-unused", "-Ywarn-dead-code", "-Ywarn-unused:-nowarn", "-Xsource:3")
  case Some((3, _)) => Seq("-Werror", "-Wunused:all", "-Wvalue-discard")
  case _ => Nil
})
ThisBuild / javacOptions ++= Seq("-target", "8")

ThisBuild / developers := List(
  Developer(
    id = "eshu",
    name = "Pavel",
    email = "tjano.xibalba@gmail.com",
    url = url("https://github.com/h8io/")
  )
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/h8io/sbt-scoverage-summary"),
    "scm:git@github.com:h8io/sbt-scoverage-summary.git"
  )
)

ThisBuild / dynverSonatypeSnapshots := true

val plugin = project
  .enablePlugins(SbtPlugin, ScoverageSummaryPlugin)
  .settings(
    name := "sbt-scoverage-summary",
    description := "SBT scoverage summary",
    sbtPlugin := true,
    sbtPluginPublishLegacyMavenStyle := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.8.0"
        case _ => "2.0.0"
      }
    },
    libraryDependencies ++= Seq(
      "org.scoverage" %% "scalac-scoverage-serializer" % "2.5.2",
      Defaults.sbtPluginExtra(
        "org.scoverage" % "sbt-scoverage" % "2.4.4",
        (pluginCrossBuild / sbtBinaryVersion).value,
        scalaBinaryVersion.value
      ),
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    ),
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq("org.scala-lang.modules" %% "scala-xml" % "2.4.0")
      case _ => Nil
    })
  )
