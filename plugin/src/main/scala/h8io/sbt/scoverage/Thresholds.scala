package h8io.sbt.scoverage

/** Coverage rate boundaries of a single metric: below `low` the rate is rendered as bad, starting from `high` it is
  * rendered as good, in between as intermediate. Statement and branch rates are unrelated to each other, so every
  * metric carries its own pair.
  */
final case class Thresholds(low: Float, high: Float)
