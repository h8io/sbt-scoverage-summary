package h8io.sbt.scoverage

import h8io.sbt.scoverage.ScoverageSummaryPlugin.autoImport.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt.*
import sbt.plugins.JvmPlugin
import scoverage.ScoverageSbtPlugin

/** The scoping of the settings is what makes a threshold left undefined in a module fall back to `ThisBuild` and then
  * to the global scope. That is not expressible in the types, so it is pinned down here.
  */
class PluginSettingsTest extends AnyFlatSpec with Matchers {
  private def labels(settings: Seq[Def.Setting[?]]) = settings.map(_.key.key.label).toSet

  "ScoverageProjectSummaryPlugin" should "be triggered by every JVM project" in {
    // Otherwise the modules of a build would carry neither the summary task nor the global threshold defaults
    ScoverageProjectSummaryPlugin.trigger shouldEqual PluginTrigger.AllRequirements
    ScoverageProjectSummaryPlugin.requires shouldEqual JvmPlugin
  }

  it should "define the thresholds and the minimums globally rather than per project" in {
    labels(ScoverageProjectSummaryPlugin.globalSettings) shouldEqual Set(
      coverageSummaryStmtLowThreshold.key.label,
      coverageSummaryStmtHighThreshold.key.label,
      coverageSummaryBranchLowThreshold.key.label,
      coverageSummaryBranchHighThreshold.key.label,
      coverageSummaryStmtMinimum.key.label,
      coverageSummaryBranchMinimum.key.label
    )
    // A project scoped default would outrank, and thereby mask, any ThisBuild override
    labels(ScoverageProjectSummaryPlugin.projectSettings) shouldEqual
      Set(ScoverageProjectSummaryPlugin.summary.key.label)
  }

  "ScoverageSummaryPlugin" should "be enabled explicitly on the aggregating project" in {
    ScoverageSummaryPlugin.trigger shouldEqual PluginTrigger.NoTrigger
    ScoverageSummaryPlugin.requires shouldEqual ScoverageSbtPlugin
  }

  it should "define both tasks and keep them from being aggregated" in {
    val settings = ScoverageSummaryPlugin.projectSettings
    labels(settings) should contain allOf (coverageSummary.key.label, coverageSummaryCheck.key.label)
    // Without these the tasks would run once more for every aggregated module which enables the plugin
    settings.count(_.key.key.label == Keys.aggregate.key.label) shouldEqual 2
  }
}
