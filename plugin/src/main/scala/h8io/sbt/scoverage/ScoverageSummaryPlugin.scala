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
    val coverageSummaryLowThreshold = settingKey[Float]("Coverage low threshold (red)")
    val coverageSummaryHighThreshold = settingKey[Float]("Coverage high threshold (green)")
    val coverageSummaryLayout = settingKey[Layout]("Summary layout")
  }
  import autoImport.*

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = ScoverageSbtPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    coverageSummary / aggregate := false,
    coverageSummaryFormat := Set(Format.GitHubFlavoredMarkdown),
    coverageSummaryLowThreshold := 50,
    coverageSummaryHighThreshold := 75,
    coverageSummaryLayout := Layout.Auto,
    coverageSummary := {
      val _ = coverageReport.all(ScopeFilter(inAggregates(ThisProject, includeRoot = true))).value
      val projects = ScoverageProjectSummaryPlugin.summary
        .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
        .value
        .flatten
      total(projects) match {
        case Some(total) =>
          for {
            format <- coverageSummaryFormat.value
            render = format.render(
              coverageSummaryLayout.value,
              coverageSummaryLowThreshold.value,
              coverageSummaryHighThreshold.value
            ) _
            filename = crossTarget.value / "scoverage-summary" / format.filename
            summary =
              "## " + name.value + " (" + thisProjectRef.value.project + ")\n### Scala " + scalaBinaryVersion.value +
                (if (sbtPlugin.value) ", SBT " + (pluginCrossBuild / sbtBinaryVersion).value else "") + "\n" +
                render(projects.sortBy(_.name), total) + "\n"
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

  // Visible for testing
  private[scoverage] def total(projects: Seq[ProjectSummary]): Option[Metrics] =
    projects.iterator.map(_.metrics).reduceOption(_ + _)
}
