package h8io.sbt.scoverage

import sbt.*
import sbt.Keys.*
import sbt.io.IO
import scoverage.ScoverageSbtPlugin

object ScoverageSummaryPlugin extends AutoPlugin {
  object autoImport {
    @transient val coverageSummary = taskKey[Seq[ProjectSummary]]("Generate scoverage summary")
    @transient val coverageSummaryCheck = taskKey[Unit]("Generate scoverage summary and fail below the minimums")
    val coverageSummaryFormat = settingKey[Set[Format]]("Summary format")
    val coverageSummaryStmtLowThreshold = settingKey[Float]("Statement coverage low threshold (red)")
    val coverageSummaryStmtHighThreshold = settingKey[Float]("Statement coverage high threshold (green)")
    val coverageSummaryBranchLowThreshold = settingKey[Float]("Branch coverage low threshold (red)")
    val coverageSummaryBranchHighThreshold = settingKey[Float]("Branch coverage high threshold (green)")
    val coverageSummaryStmtMinimum =
      settingKey[Option[Float]]("Statement coverage minimum, defaults to the low threshold")
    val coverageSummaryBranchMinimum =
      settingKey[Option[Float]]("Branch coverage minimum, defaults to the low threshold")
    val coverageSummaryLayout = settingKey[Layout]("Summary layout")
  }
  import autoImport.*

  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = ScoverageSbtPlugin

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(
      coverageSummary / aggregate := false,
      coverageSummaryCheck / aggregate := false,
      coverageSummaryFormat := Set(Format.GitHubFlavoredMarkdown),
      coverageSummaryLayout := Layout.Auto,
      coverageSummary := {
        val projects = ScoverageProjectSummaryPlugin.summary
          .all(ScopeFilter(inAggregates(ThisProject, includeRoot = true)))
          .value
          .flatten
        val scope = thisProjectRef.value.project
        // The total row is judged by the values of the aggregating project, which are not necessarily those of any
        // single module.
        val summarize = ScoverageProjectSummaryPlugin.summarize.value
        total(projects) match {
          case Some(metrics) =>
            val total = summarize(metrics)
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
        projects
      },
      // Depending on the value of coverageSummary rather than merely on its completion means the coverage data is
      // deserialized once, however many tasks consume it within a single evaluation, and that the report is on disk
      // before this task can fail.
      coverageSummaryCheck := {
        val projects = coverageSummary.value
        val log = streams.value.log
        val scope = thisProjectRef.value.project
        val summarize = ScoverageProjectSummaryPlugin.summarize.value
        incoherent(projects) foreach (message => log.warn(message))
        total(projects) foreach { metrics =>
          val failures = violations(projects, scope, summarize(metrics))
          if (failures.nonEmpty) {
            failures foreach (message => log.error(message))
            throw new MessageOnlyException(s"Coverage is below the minimum in ${failures.size} case(s)")
          }
        }
      }
    )

  // Visible for testing
  private[scoverage] def total(projects: Seq[ProjectSummary]): Option[Metrics] =
    projects.iterator.map(_.summary.metrics).reduceOption(_ + _)

  // Visible for testing
  // The total is checked as well as the modules. It is not implied by them: the total rate is a weighted mean of the
  // module rates, so modules passing a shared minimum would guarantee it, but a module exempted with a minimum of its
  // own can drag the total below the minimum of the aggregating project while every module still passes.
  private[scoverage] def violations(projects: Seq[ProjectSummary], totalScope: String, total: Summary): Seq[String] =
    scopes(projects, totalScope, total) flatMap { case (scope, summary) =>
      violation(scope, "Statement", summary.metrics.statementRate, summary.minimum.statements).toSeq ++
        violation(scope, "Branch", summary.metrics.branchRate, summary.minimum.branches)
    }

  // A metric with nothing to cover has no rate and cannot fail; the report renders it as a dash for the same reason.
  private def violation(scope: String, metric: String, rate: Option[Float], minimum: Float): Option[String] =
    rate filter (_ < minimum) map { rate =>
      f"[$scope] $metric coverage is $rate%2.02f%%, below the required $minimum%2.02f%%"
    }

  // Visible for testing
  // A minimum above the low threshold is legal but worth pointing out: the offending cell is rendered in yellow or
  // green while the build fails, which is impossible to explain to whoever reads the report.
  private[scoverage] def incoherent(projects: Seq[ProjectSummary]): Seq[String] =
    projects flatMap { project =>
      val summary = project.summary
      incoherence(project.id, "Stmt", summary.minimum.statements, summary.statements.low).toSeq ++
        incoherence(project.id, "Branch", summary.minimum.branches, summary.branches.low)
    }

  private def incoherence(scope: String, metric: String, minimum: Float, low: Float): Option[String] =
    if (minimum <= low) None
    else
      Some(
        f"[$scope] coverageSummary${metric}Minimum ($minimum%2.02f%%) is above coverageSummary${metric}LowThreshold " +
          f"($low%2.02f%%), so the metric can fail the check without being rendered in red"
      )

  // Visible for testing
  // Every module resolves its own values, so all of them are validated and every violation is reported at once.
  // `totalScope` labels the values of the aggregating project; when that project is aggregated into the report as
  // well, the two labels coincide and the duplicate message collapses.
  private[scoverage] def validate(projects: Seq[ProjectSummary], totalScope: String, total: Summary): Seq[String] =
    (scopes(projects, totalScope, total) flatMap { case (scope, summary) =>
      (validateThresholds("Stmt", summary.statements).toSeq ++
        validateThresholds("Branch", summary.branches) ++
        validateMinimum("Stmt", summary.minimum.statements) ++
        validateMinimum("Branch", summary.minimum.branches)) map (message => s"[$scope] $message")
    }).distinct

  private def scopes(
      projects: Seq[ProjectSummary],
      totalScope: String,
      total: Summary
  ): Seq[(String, Summary)] = projects.map(project => project.id -> project.summary) :+ (totalScope -> total)

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

  // Visible for testing
  private[scoverage] def validateMinimum(metric: String, minimum: Float): Option[String] =
    if (0 <= minimum && minimum <= 100) None
    else Some(s"coverageSummary${metric}Minimum ($minimum) must be within [0, 100]")
}
