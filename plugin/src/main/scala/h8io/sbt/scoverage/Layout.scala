package h8io.sbt.scoverage

sealed trait Layout

object Layout {
  case object Auto extends Layout
  case object Multi extends Layout
  case object Total extends Layout
}
