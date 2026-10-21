package app

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GreetingTest extends AnyFlatSpec with Matchers {
  "Greeting" should "say hello" in {
    Greeting.hello("world") shouldBe "hello, world"
  }
}
