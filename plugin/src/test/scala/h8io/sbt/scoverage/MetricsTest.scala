package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetricsTest extends AnyFlatSpec with Matchers {
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
}
