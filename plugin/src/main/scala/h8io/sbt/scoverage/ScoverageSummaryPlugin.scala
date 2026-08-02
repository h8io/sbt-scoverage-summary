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
      coverageSummaryLayout := Layout.Auto,
      coverageSummary := {
        val projects = ScoverageProjectSummaryPlugin.summary
          .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
          .value
          .flatten
        val scope = thisProjectRef.value.project
        // The total row is judged by the thresholds of the aggregating project, which are not necessarily those of any
        // single module.
        val statements = Thresholds(coverageSummaryStmtLowThreshold.value, coverageSummaryStmtHighThreshold.value)
        val branches = Thresholds(coverageSummaryBranchLowThreshold.value, coverageSummaryBranchHighThreshold.value)
        total(projects) match {
          case Some(metrics) =>
            val total = Summary(metrics, statements, branches)
            val errors = validate(projects, scope, total)
            if (errors.nonEmpty) throw new MessageOnlyException(errors.mkString("\n"))
            for {
              format <- coverageSummaryFormat.value
              render = format.render(coverageSummaryLayout.value)(_, _)
              filename = crossTarget.value / "scoverage-summary" / format.filename
              summary =
                "## " + name.value + " (" + scope + ")\n### Scala " + scalaBinaryVersion.value +
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
    projects.iterator.map(_.summary.metrics).reduceOption(_ + _)

  // Visible for testing
  // Every module resolves its own thresholds, so all of them are validated and every violation is reported at once.
  // `totalScope` labels the thresholds of the aggregating project; when that project is aggregated into the report as
  // well, the two labels coincide and the duplicate message collapses.
  private[scoverage] def validate(projects: Seq[ProjectSummary], totalScope: String, total: Summary): Seq[String] =
    ((projects.map(project => project.id -> project.summary) :+ (totalScope -> total)) flatMap {
      case (scope, summary) =>
        (validateThresholds("Stmt", summary.statements).toSeq ++ validateThresholds("Branch", summary.branches))
          .map(message => s"[$scope] $message")
    }).distinct

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
