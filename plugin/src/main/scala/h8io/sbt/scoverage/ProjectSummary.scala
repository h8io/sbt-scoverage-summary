package h8io.sbt.scoverage

import sbt.ProjectRef
import scoverage.domain.Coverage

final case class ProjectSummary(id: String, name: String, metrics: Metrics)

object ProjectSummary {
  def apply(ref: ProjectRef, name: String, coverage: Coverage): ProjectSummary =
    ProjectSummary(ref.project, name, Metrics(coverage))
}
