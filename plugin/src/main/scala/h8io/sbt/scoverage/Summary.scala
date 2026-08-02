package h8io.sbt.scoverage

/** Coverage of a single scope together with the thresholds it is judged by. Thresholds travel with the metrics rather
  * than being passed to a format separately, because every module resolves its own values and a table may therefore mix
  * several sets of them.
  */
final case class Summary(metrics: Metrics, statements: Thresholds, branches: Thresholds)
