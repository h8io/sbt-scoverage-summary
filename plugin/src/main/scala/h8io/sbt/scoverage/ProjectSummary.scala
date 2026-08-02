package h8io.sbt.scoverage

import sbt.ProjectRef
import scoverage.domain.Coverage

final case class ProjectSummary(id: String, name: String, summary: Summary)

object ProjectSummary {
  def apply(
      ref: ProjectRef,
      name: String,
      coverage: Coverage,
      statements: Thresholds,
      branches: Thresholds
  ): ProjectSummary = ProjectSummary(ref.project, name, Summary(Metrics(coverage), statements, branches))
}
