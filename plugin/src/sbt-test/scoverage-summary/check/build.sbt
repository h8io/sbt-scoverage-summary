ThisBuild / coverageSummaryStmtLowThreshold := 90
ThisBuild / coverageSummaryStmtHighThreshold := 95
ThisBuild / coverageSummaryBranchLowThreshold := 90
ThisBuild / coverageSummaryBranchHighThreshold := 95

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test

val root = (project in file("."))
  .enablePlugins(ScoverageSummaryPlugin)
  .settings(name := "check", scalaVersion := "3.3.6")

TaskKey[Unit]("checkReport") := {
  val report = crossTarget.value / "scoverage-summary" / "gfm.md"
  assert(report.exists, s"$report was not written")
  assert(IO.read(report).contains("## check (root)"), IO.read(report))
}
