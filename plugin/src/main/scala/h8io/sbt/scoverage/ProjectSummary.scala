package h8io.sbt.scoverage

import sbt.ProjectRef

final case class ProjectSummary(id: String, name: String, summary: Summary)

object ProjectSummary {
  def apply(ref: ProjectRef, name: String, summary: Summary): ProjectSummary =
    ProjectSummary(ref.project, name, summary)
}
