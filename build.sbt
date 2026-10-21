dynverSonatypeSnapshots := true
dynverSeparator := "-"

ThisBuild / coverageSummaryStmtLowThreshold := 90
ThisBuild / coverageSummaryStmtHighThreshold := 95
ThisBuild / coverageSummaryBranchLowThreshold := 90
ThisBuild / coverageSummaryBranchHighThreshold := 95

val plugin = projectMatrix.in(file("plugin"))
  .jvmPlatform(scalaVersions = Seq("3.9.0", "2.12.21"))
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
    // Scala 3.9 emits TASTy that the Scala 3.8 under every sbt 2.0.x cannot read, so the sbt 2 row has to be built,
    // and tested, against an sbt that runs on 3.9 itself; 2.1.0-RC1 is the first candidate release that does.
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.8.0"
        case _ => "2.1.0-RC1"
      }
    },
    scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false,
    // sbt 1.8.0 brings Scala 2.12.17, which cannot read Java 21 class files and dies with "bad constant pool index"
    // before the test build is even loaded; the plugin runs on any sbt 1.x, so a current one is used instead.
    scriptedSbt :=
      (scalaBinaryVersion.value match {
        case "2.12" => "1.12.14"
        case _ => (pluginCrossBuild / sbtVersion).value
      }),
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
      "org.scalamock" %% "scalamock-scalatest" % "7.6.0" % Test
    ),
    libraryDependencies ++=
      (scalaBinaryVersion.value match {
        case "3" => Seq("org.scala-lang.modules" %% "scala-xml" % "2.5.0")
        case _ => Nil
      })
  )
