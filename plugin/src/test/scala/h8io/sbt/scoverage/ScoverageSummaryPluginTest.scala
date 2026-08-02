package h8io.sbt.scoverage

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScoverageSummaryPluginTest extends AnyFlatSpec with Matchers with OptionValues {
  private val noMinimum = Minimum(0, 0)
  private val valid = Thresholds(50, 75)
  private val invalid = Thresholds(75, 50)

  private def project(id: String, statements: Thresholds = valid, branches: Thresholds = valid) =
    ProjectSummary(id, id, Summary(Metrics(3, 2, 1, 5, 4), statements, branches, noMinimum))

  "total" should "return None for an empty sequence" in {
    ScoverageSummaryPlugin.total(Nil) shouldBe None
  }

  it should "return a summary metric for a non-empty sequence" in {
    ScoverageSummaryPlugin.total(
      ProjectSummary("project1", "project-1", Summary(Metrics(3, 2, 1, 5, 4), valid, valid, noMinimum)) ::
        ProjectSummary("project2", "project-2", Summary(Metrics(7, 5, 3, 9, 7), valid, valid, noMinimum)) ::
        ProjectSummary("project3", "project-3", Summary(Metrics(9, 7, 5, 13, 11), valid, valid, noMinimum)) ::
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

  private val total = Summary(Metrics(3, 2, 1, 5, 4), valid, valid, noMinimum)

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
      ScoverageSummaryPlugin.validate(Nil, "root", Summary(Metrics(3, 2, 1, 5, 4), invalid, valid, noMinimum))
    errors should have size 1
    errors.head should include("[root]")
  }

  it should "not report the aggregating project twice when it is aggregated into itself" in {
    val root = project("root", statements = invalid)
    ScoverageSummaryPlugin.validate(Seq(root), "root", root.summary) should have size 1
  }

  it should "reject a minimum outside the range of possible coverage rates" in {
    ScoverageSummaryPlugin.validateMinimum("Stmt", 101) should not be empty
    ScoverageSummaryPlugin.validateMinimum("Stmt", -1) should not be empty
    ScoverageSummaryPlugin.validateMinimum("Stmt", 0) shouldBe None
    ScoverageSummaryPlugin.validateMinimum("Stmt", 100) shouldBe None
  }

  // 5 of 10 statements and 5 of 10 branches, that is 50% of each
  private def half(id: String, minimum: Minimum) =
    ProjectSummary(id, id, Summary(Metrics(10, 5, 0, 10, 5), valid, valid, minimum))

  private def totalOf(projects: Seq[ProjectSummary], minimum: Minimum) =
    Summary(ScoverageSummaryPlugin.total(projects).value, valid, valid, minimum)

  "violations" should "accept coverage above the minimum" in {
    val projects = Seq(half("a", Minimum(40, 40)))
    ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(40, 40))) shouldBe empty
  }

  it should "accept coverage exactly at the minimum" in {
    val projects = Seq(half("a", Minimum(50, 50)))
    ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(50, 50))) shouldBe empty
  }

  it should "reject coverage below the minimum" in {
    val projects = Seq(half("a", Minimum(50.01f, 0)))
    val failures = ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(0, 0)))
    failures should have size 1
    failures.head should (include("[a]") and include("Statement"))
  }

  it should "reject every offending module and metric rather than stopping at the first" in {
    val projects = Seq(half("a", Minimum(90, 90)), half("b", Minimum(0, 0)), half("c", Minimum(90, 0)))
    ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(0, 0))) should have size 3
  }

  it should "not fail a metric which has nothing to cover" in {
    val branchless = ProjectSummary("a", "a", Summary(Metrics(10, 10, 0, 0, 0), valid, valid, Minimum(100, 100)))
    val projects = Seq(branchless)
    ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(100, 100))) shouldBe empty
  }

  it should "let a minimum of 100 be reached by full coverage" in {
    val full = ProjectSummary("a", "a", Summary(Metrics(10, 10, 0, 10, 10), valid, valid, Minimum(100, 100)))
    val projects = Seq(full)
    ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(100, 100))) shouldBe empty
  }

  it should "reject a total dragged down by an exempted module even when every module passes" in {
    val good = ProjectSummary("good", "good", Summary(Metrics(10, 10, 0, 10, 10), valid, valid, Minimum(90, 90)))
    val exempt = ProjectSummary("exempt", "exempt", Summary(Metrics(10, 0, 0, 10, 0), valid, valid, Minimum(0, 0)))
    val projects = Seq(good, exempt)
    val failures = ScoverageSummaryPlugin.violations(projects, "root", totalOf(projects, Minimum(90, 90)))
    failures should have size 2
    all(failures) should include("[root]")
  }

  "incoherent" should "stay silent when the minimum is not above the low threshold" in {
    ScoverageSummaryPlugin.incoherent(Seq(half("a", Minimum(50, 50)), half("b", Minimum(0, 0)))) shouldBe empty
  }

  it should "warn about a minimum above the low threshold" in {
    val warnings = ScoverageSummaryPlugin.incoherent(Seq(half("a", Minimum(80, 50))))
    warnings should have size 1
    warnings.head should
      (include("[a]") and include("coverageSummaryStmtMinimum") and
        include("coverageSummaryStmtLowThreshold"))
  }
}
