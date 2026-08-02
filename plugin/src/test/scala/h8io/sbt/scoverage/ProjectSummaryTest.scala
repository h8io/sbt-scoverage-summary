package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt.ProjectRef

import java.io.File

class ProjectSummaryTest extends AnyFlatSpec with Matchers {
  "apply" should "take the id from the project reference" in {
    val summary = Summary(Metrics(3, 2, 1, 5, 4), Thresholds(50, 75), Thresholds(50, 75), Minimum(0, 0))
    val reference = ProjectRef(new File("build").toURI, "the-id")
    ProjectSummary(reference, "the-name", summary) shouldEqual ProjectSummary("the-id", "the-name", summary)
  }
}
