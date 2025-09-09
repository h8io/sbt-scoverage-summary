package h8io.sbt.scoverage

import sbt.*
import sbt.Keys.{baseDirectory, crossTarget, name, thisProjectRef}
import sbt.plugins.JvmPlugin
import scoverage.domain.Constants
import scoverage.reporter.IOUtils
import scoverage.serialize.Serializer

object ScoverageProjectSummaryPlugin extends AutoPlugin {
  private[scoverage] val summary = taskKey[Option[ProjectSummary]]("summary")

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    summary := {
      val dataDir = crossTarget.value / Constants.DataDir
      if (dataDir.exists()) {
        val coverage = Serializer.deserialize(Serializer.coverageFile(dataDir), baseDirectory.value)
        coverage(IOUtils.invoked(IOUtils.findMeasurementFiles(dataDir).toIndexedSeq))
        Some(ProjectSummary(thisProjectRef.value, name.value, coverage))
      } else None
    }
  )
}
