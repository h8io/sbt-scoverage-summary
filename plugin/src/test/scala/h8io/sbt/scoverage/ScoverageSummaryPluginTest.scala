package h8io.sbt.scoverage

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScoverageSummaryPluginTest extends AnyFlatSpec with Matchers with OptionValues {
  private val valid = Thresholds(50, 75)
  private val invalid = Thresholds(75, 50)

  private def project(id: String, statements: Thresholds = valid, branches: Thresholds = valid) =
    ProjectSummary(id, id, Summary(Metrics(3, 2, 1, 5, 4), statements, branches))

  "total" should "return None for an empty sequence" in {
    ScoverageSummaryPlugin.total(Nil) shouldBe None
  }

  it should "return a summary metric for a non-empty sequence" in {
    ScoverageSummaryPlugin.total(
      ProjectSummary("project1", "project-1", Summary(Metrics(3, 2, 1, 5, 4), valid, valid)) ::
        ProjectSummary("project2", "project-2", Summary(Metrics(7, 5, 3, 9, 7), valid, valid)) ::
        ProjectSummary("project3", "project-3", Summary(Metrics(9, 7, 5, 13, 11), valid, valid)) ::
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

  private val total = Summary(Metrics(3, 2, 1, 5, 4), valid, valid)

  "validate" should "accept modules which all resolve consistent thresholds" in {
    ScoverageSummaryPlugin.validate(Seq(project("a"), project("b")), "root", total) shouldBe empty
  }

  it should "report every offending module rather than stopping at the first one" in {
    val projects = Seq(project("a", statements = invalid), project("b"), project("c", branches = invalid))
    val errors = ScoverageSummaryPlugin.validate(projects, "root", total)
    errors should have size 2
    exactly(1, errors) should include("[a]")
    exactly(1, errors) should include("[c]")
  }

  it should "report both metrics of the same module" in {
    ScoverageSummaryPlugin.validate(
      Seq(project("a", statements = invalid, branches = invalid)),
      "root",
      total
    ) should have size 2
  }

  it should "report the thresholds of the aggregating project" in {
    val errors =
      ScoverageSummaryPlugin.validate(Nil, "root", Summary(Metrics(3, 2, 1, 5, 4), invalid, valid))
    errors should have size 1
    errors.head should include("[root]")
  }

  it should "not report the aggregating project twice when it is aggregated into itself" in {
    val root = project("root", statements = invalid)
    ScoverageSummaryPlugin.validate(Seq(root), "root", root.summary) should have size 1
  }
}
