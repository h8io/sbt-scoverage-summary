package h8io.sbt.scoverage

import sbt.*
import sbt.Keys.*
import sbt.io.IO
import scoverage.ScoverageKeys.coverageReport
import scoverage.ScoverageSbtPlugin

object ScoverageSummaryPlugin extends AutoPlugin {
  object autoImport {
    val coverageSummary = taskKey[Unit]("Generate scoverage summary")
    val coverageSummaryFormat = settingKey[Set[Format]]("Summary format")
    val coverageLowThreshold = settingKey[Float]("Coverage low threshold (red)")
    val coverageHighThreshold = settingKey[Float]("Coverage high threshold (green)")
  }
  import autoImport.*

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = ScoverageSbtPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    coverageSummary / aggregate := false,
    coverageSummaryFormat := Set(Format.GitHubFlavoredMarkdown),
    coverageLowThreshold := 50,
    coverageHighThreshold := 75,
    coverageSummary := {
      val _ = coverageReport.all(ScopeFilter(inAggregates(ThisProject, includeRoot = true))).value
      val projects = ScoverageProjectSummaryPlugin.summary
        .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
        .value
        .flatten
      projects.iterator.map(_.metrics).reduceOption(_ + _) match {
        case Some(total) =>
          for {
            format <- coverageSummaryFormat.value
            filename = crossTarget.value / "scoverage-summary" / format.filename
            summary =
              "# Scala " + scalaBinaryVersion.value +
                (if (sbtPlugin.value) ", SBT " + (pluginCrossBuild / sbtBinaryVersion).value else "") + "\n" +
                format.render(
                  projects.sortBy(_.name),
                  total,
                  coverageLowThreshold.value,
                  coverageHighThreshold.value
                ) + "\n"
          } {
            IO.write(filename, summary)
            streams.value.log.info(s"Scoverage summary report (${format.name}) written to $filename")
          }
        case None =>
          streams.value.log.warn(
            s"[sbt-scoverage-summary] No coverage data found for project '" +
              thisProject.value.id + "' or any of its aggregated modules"
          )
      }
    }
  )
}
