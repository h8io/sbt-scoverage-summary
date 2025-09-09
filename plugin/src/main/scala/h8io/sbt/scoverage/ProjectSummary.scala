package h8io.sbt.scoverage

import sbt.ProjectRef
import scoverage.domain.Coverage

final case class ProjectSummary(ref: ProjectRef, name: String, metrics: Metrics)

object ProjectSummary {
  def apply(ref: ProjectRef, name: String, coverage: Coverage): ProjectSummary =
    ProjectSummary(ref, name, Metrics(coverage))
}
