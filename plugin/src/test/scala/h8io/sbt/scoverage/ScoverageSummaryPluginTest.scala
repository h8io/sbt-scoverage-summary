package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScoverageSummaryPluginTest extends AnyFlatSpec with Matchers {
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
}
