package app

object Greeting {
  def hello(name: String): String = s"hello, $name"

  // Never called, so the statement rate stays well below the bar the build sets
  def goodbye(name: String): String = s"goodbye, $name"
}
