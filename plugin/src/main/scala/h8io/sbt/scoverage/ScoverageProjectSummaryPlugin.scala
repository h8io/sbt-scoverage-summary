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
      coverageSummaryBranchHighThreshold := 75
    )

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(
      summary := {
        val dataDir = crossTarget.value / Constants.DataDir
        if (dataDir.exists()) {
          val coverage = Serializer.deserialize(Serializer.coverageFile(dataDir), baseDirectory.value)
          coverage(IOUtils.invoked(IOUtils.findMeasurementFiles(dataDir).toIndexedSeq))
          Some(
            ProjectSummary(
              thisProjectRef.value,
              name.value,
              coverage,
              Thresholds(coverageSummaryStmtLowThreshold.value, coverageSummaryStmtHighThreshold.value),
              Thresholds(coverageSummaryBranchLowThreshold.value, coverageSummaryBranchHighThreshold.value)
            )
          )
        } else None
      }
    )
}
