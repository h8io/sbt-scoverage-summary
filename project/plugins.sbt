addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")
dependencyOverrides += "org.scoverage" % "scalac-scoverage-plugin_2.12.21" % "2.5.1"
addSbtPlugin("io.h8.sbt" % "sbt-scoverage-summary" % "1.0.4")
