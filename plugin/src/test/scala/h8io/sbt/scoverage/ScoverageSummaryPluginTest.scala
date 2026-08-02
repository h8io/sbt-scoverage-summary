package h8io.sbt.scoverage

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScoverageSummaryPluginTest extends AnyFlatSpec with Matchers with OptionValues {
  "total" should "return None for an empty sequence" in {
    ScoverageSummaryPlugin.total(Nil) shouldBe None
  }

  it should "return a summary metric for a non-empty sequence" in {
    ScoverageSummaryPlugin.total(
      ProjectSummary("project1", "project-1", Metrics(3, 2, 1, 5, 4)) ::
        ProjectSummary("project2", "project-2", Metrics(7, 5, 3, 9, 7)) ::
        ProjectSummary("project3", "project-3", Metrics(9, 7, 5, 13, 11)) ::
        Nil
    ) shouldBe Some(Metrics(19, 14, 9, 27, 22))
  }

  "validateThresholds" should "accept a low threshold below the high one" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(50, 75)) shouldBe None
  }

  it should "accept equal thresholds as a two-color scale" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(60, 60)) shouldBe None
  }

  it should "accept the whole range of possible coverage rates" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(0, 100)) shouldBe None
  }

  it should "reject a low threshold above the high one" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(75, 50)) should not be empty
  }

  it should "reject a negative low threshold" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(-1, 75)) should not be empty
  }

  it should "reject a high threshold above the maximal coverage rate" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(50, 101)) should not be empty
  }

  it should "reject a threshold which is not a number" in {
    ScoverageSummaryPlugin.validateThresholds("Stmt", Thresholds(Float.NaN, 75)) should not be empty
  }

  it should "point at the setting keys of the metric being validated" in {
    ScoverageSummaryPlugin.validateThresholds("Branch", Thresholds(75, 50)).value should (
      include("coverageSummaryBranchLowThreshold") and include("coverageSummaryBranchHighThreshold")
    )
  }
}
