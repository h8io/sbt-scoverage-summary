package h8io.sbt.scoverage

/** Coverage rates below which `coverageSummaryCheck` fails. Kept apart from [[Thresholds]] because a threshold only
  * picks a color while a minimum blocks the build, even though by default a minimum equals the low threshold of its
  * metric and the two therefore agree.
  */
final case class Minimum(statements: Float, branches: Float)
