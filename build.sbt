import sbt.url

ThisBuild / organization := "io.h8.sbt"
ThisBuild / organizationName := "H8IO"
ThisBuild / organizationHomepage := Some(url("https://github.com/h8io/"))

ThisBuild / licenses := List("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalaVersion := "2.12.20"
// ThisBuild / crossScalaVersions += "3.7.2"

ThisBuild / scalacOptions ++= Seq("--deprecation", "--feature", "--unchecked", "-Xlint:_", "-Xfatal-warnings")
ThisBuild / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 12)) => Seq("-Ywarn-unused", "-Ywarn-dead-code", "-Ywarn-unused:-nowarn", "-Xsource:3")
  case _ => ???
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

scmInfo := Some(
  ScmInfo(
    url("https://github.com/h8io/sbt-scoverage-summary"),
    "scm:git@github.com:h8io/sbt-scoverage-summary.git"
  )
)

val plugin = project
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scoverage-summary",
    description := "SBT scoverage summary",
    homepage := Some(url("https://github.com/h8io/sbt-scoverage-summary")),
    sbtPlugin := true,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.11.5"
        case _ => "2.0.0-RC4"
      }
    },
    addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.3.1"),
    libraryDependencies ++= Seq(
      "org.scoverage" %% "scalac-scoverage-serializer" % "2.3.0",
      "org.scala-sbt" % "sbt" % (pluginCrossBuild / sbtVersion).value % Provided
    )
  )
