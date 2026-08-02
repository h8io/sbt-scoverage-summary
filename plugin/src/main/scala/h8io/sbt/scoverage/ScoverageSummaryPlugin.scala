package h8io.sbt.scoverage

import sbt.*
import sbt.Keys.*
import sbt.io.IO
import scoverage.ScoverageSbtPlugin

object ScoverageSummaryPlugin extends AutoPlugin {
  object autoImport {
    @transient val coverageSummary = taskKey[Unit]("Generate scoverage summary")
    val coverageSummaryFormat = settingKey[Set[Format]]("Summary format")
    val coverageSummaryStmtLowThreshold = settingKey[Float]("Statement coverage low threshold (red)")
    val coverageSummaryStmtHighThreshold = settingKey[Float]("Statement coverage high threshold (green)")
    val coverageSummaryBranchLowThreshold = settingKey[Float]("Branch coverage low threshold (red)")
    val coverageSummaryBranchHighThreshold = settingKey[Float]("Branch coverage high threshold (green)")
    val coverageSummaryLayout = settingKey[Layout]("Summary layout")
  }
  import autoImport.*

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = ScoverageSbtPlugin

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(
      coverageSummary / aggregate := false,
      coverageSummaryFormat := Set(Format.GitHubFlavoredMarkdown),
      coverageSummaryStmtLowThreshold := 50,
      coverageSummaryStmtHighThreshold := 75,
      coverageSummaryBranchLowThreshold := 50,
      coverageSummaryBranchHighThreshold := 75,
      coverageSummaryLayout := Layout.Auto,
      coverageSummary := {
        val statements = Thresholds(coverageSummaryStmtLowThreshold.value, coverageSummaryStmtHighThreshold.value)
        val branches = Thresholds(coverageSummaryBranchLowThreshold.value, coverageSummaryBranchHighThreshold.value)
        val errors = validateThresholds("Stmt", statements).toSeq ++ validateThresholds("Branch", branches)
        if (errors.nonEmpty) throw new MessageOnlyException(errors.mkString("\n"))
        val projects = ScoverageProjectSummaryPlugin.summary
          .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
          .value
          .flatten
        total(projects) match {
          case Some(total) =>
            for {
              format <- coverageSummaryFormat.value
              render = format.render(coverageSummaryLayout.value, statements, branches)(_, _)
              filename = crossTarget.value / "scoverage-summary" / format.filename
              summary =
                "## " + name.value + " (" + thisProjectRef.value.project + ")\n### Scala " + scalaBinaryVersion.value +
                  (if (sbtPlugin.value) ", SBT " + (pluginCrossBuild / sbtBinaryVersion).value else "") + "\n" +
                  render(projects.sortBy(_.name), total) + "\n\n"
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

  // Visible for testing
  // Coverage rates are always within [0, 100], so a threshold outside that range makes a color unreachable,
  // and a low threshold above the high one leaves no room for the intermediate color at all.
  // Equal thresholds are allowed: they define a two-color scale without an intermediate band.
  // `metric` is the setting key infix, so that the message points at the keys the user has to fix.
  private[scoverage] def validateThresholds(metric: String, thresholds: Thresholds): Option[String] =
    if (0 <= thresholds.low && thresholds.low <= thresholds.high && thresholds.high <= 100) None
    else
      Some(
        s"Inconsistent thresholds: coverageSummary${metric}LowThreshold (${thresholds.low}) and " +
          s"coverageSummary${metric}HighThreshold (${thresholds.high}) must satisfy 0 <= low <= high <= 100"
      )
}
