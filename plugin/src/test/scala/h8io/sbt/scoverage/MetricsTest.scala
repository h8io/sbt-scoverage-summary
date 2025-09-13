package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MetricsTest extends AnyFlatSpec with Matchers {
  "operator +" should "produce a correct sum of metrics" in {
    Metrics(42, 33, 5, 64, 47) + Metrics(77, 59, 2, 91, 37) shouldEqual Metrics(119, 92, 7, 155, 84)
  }
}
