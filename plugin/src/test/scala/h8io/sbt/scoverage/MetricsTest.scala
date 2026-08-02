package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scoverage.domain.ClassType
import scoverage.domain.Coverage
import scoverage.domain.Location
import scoverage.domain.Statement

class MetricsTest extends AnyFlatSpec with Matchers {
  private def statement(id: Int, branch: Boolean, invoked: Boolean) = {
    val location = Location("pkg", "Cls", "pkg.Cls", ClassType.Object, "method", "Cls.scala")
    Statement(location, id, id, id, id, "", "", "", branch, if (invoked) 1 else 0)
  }

  "operator +" should "produce a correct sum of metrics" in {
    Metrics(42, 33, 5, 64, 47) + Metrics(77, 59, 2, 91, 37) shouldEqual Metrics(119, 92, 7, 155, 84)
  }

  "rates" should "be percentages of the invoked code" in {
    val metrics = Metrics(8, 2, 0, 5, 1)
    metrics.statementRate shouldBe Some(25f)
    metrics.branchRate shouldBe Some(20f)
  }

  it should "be undefined when there is nothing to cover" in {
    val metrics = Metrics(0, 0, 0, 0, 0)
    metrics.statementRate shouldBe None
    metrics.branchRate shouldBe None
  }

  it should "be exactly 100 for fully covered code, so that a minimum of 100 is reachable" in {
    val metrics = Metrics(7, 7, 0, 3, 3)
    metrics.statementRate shouldBe Some(100f)
    metrics.branchRate shouldBe Some(100f)
  }

  "apply" should "count the statements of a scoverage coverage" in {
    val coverage = Coverage()
    coverage.add(statement(1, branch = false, invoked = true))
    coverage.add(statement(2, branch = false, invoked = false))
    coverage.add(statement(3, branch = true, invoked = true))
    coverage.add(statement(4, branch = true, invoked = false))
    coverage.addIgnoredStatement(statement(5, branch = false, invoked = false))
    // Ignored statements are held apart from the counted ones, so they stay out of the denominator
    Metrics(coverage) shouldEqual Metrics(4, 2, 1, 2, 1)
  }
}
