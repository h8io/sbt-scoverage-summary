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
  }
  import autoImport.*

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = ScoverageSbtPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    coverageSummary / aggregate := false,
    coverageSummaryFormat := Set(Format.GitHubFlavoredMarkdown),
    coverageSummary := {
      coverageReport.all(ScopeFilter(inAggregates(ThisProject, includeRoot = true))).value
      val projects = ScoverageProjectSummaryPlugin.summary
        .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
        .value
        .flatten
      projects.iterator.map(_.metrics).reduceOption(_ + _) match {
        case Some(total) =>
          for {
            format <- coverageSummaryFormat.value
            summary = format.render(projects.sortBy(_.name), total)
          } IO.write(crossTarget.value / "scoverage-summary" / ("summary" + format.fileSuffix), summary)
        case None =>
          streams.value.log.warn(
            s"[sbt-scoverage-summary] No coverage data found for project '" +
              thisProject.value.id + "' or any of its aggregated modules"
          )
      }
    }
  )
}
