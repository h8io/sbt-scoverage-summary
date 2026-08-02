package h8io.sbt.scoverage

import h8io.sbt.scoverage.ScoverageSummaryPlugin.autoImport.*
import sbt.*
import sbt.Keys.{baseDirectory, crossTarget, name, thisProjectRef}
import sbt.plugins.JvmPlugin
import scoverage.domain.Constants
import scoverage.reporter.IOUtils
import scoverage.serialize.Serializer

object ScoverageProjectSummaryPlugin extends AutoPlugin {
  @transient private[scoverage] val summary = taskKey[Option[ProjectSummary]]("summary")

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin

  // Threshold defaults live in the global scope rather than in project settings, so that they are the last link of the
  // `project -> ThisBuild -> Global` delegation chain: a module which defines none of them still resolves a value,
  // while `ThisBuild / ...` and `someModule / ...` both take precedence. Defining them per project would instead give
  // every module an explicit value of its own and silently mask any `ThisBuild` override.
  // They belong to this plugin, which is always active, because `summary` below reads them in every module regardless
  // of where ScoverageSummaryPlugin is enabled.
  override def globalSettings: Seq[Def.Setting[?]] =
    Seq(
      coverageSummaryStmtLowThreshold := 50,
      coverageSummaryStmtHighThreshold := 75,
      coverageSummaryBranchLowThreshold := 50,
      coverageSummaryBranchHighThreshold := 75,
      coverageSummaryStmtMinimum := None,
      coverageSummaryBranchMinimum := None
    )

  // A minimum left undefined follows the low threshold of its own metric. That fallback cannot be expressed as a
  // setting default: defined globally it would resolve the threshold globally too and miss per-module overrides, and
  // `Def.derive` would place it in the project scope, where it outranks and thereby masks `ThisBuild / ...`. Resolving
  // it here instead keeps both, because every module evaluates this in its own scope.
  private[scoverage] lazy val summarize: Def.Initialize[Metrics => Summary] = Def.setting {
    val statements = Thresholds(coverageSummaryStmtLowThreshold.value, coverageSummaryStmtHighThreshold.value)
    val branches = Thresholds(coverageSummaryBranchLowThreshold.value, coverageSummaryBranchHighThreshold.value)
    val minimum = Minimum(
      coverageSummaryStmtMinimum.value getOrElse statements.low,
      coverageSummaryBranchMinimum.value getOrElse branches.low
    )
    metrics => Summary(metrics, statements, branches, minimum)
  }

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(
      summary := {
        val dataDir = crossTarget.value / Constants.DataDir
        val toSummary = summarize.value
        if (dataDir.exists()) {
          val coverage = Serializer.deserialize(Serializer.coverageFile(dataDir), baseDirectory.value)
          coverage(IOUtils.invoked(IOUtils.findMeasurementFiles(dataDir).toIndexedSeq))
          Some(ProjectSummary(thisProjectRef.value, name.value, toSummary(Metrics(coverage))))
        } else None
      }
    )
}
