package h8io.sbt.scoverage

import scala.xml.Elem

trait Format {
  def name: String

  private[scoverage] final def render(layout: Layout)(projects: Seq[ProjectSummary], total: Summary): String =
    layout match {
      case Layout.Auto =>
        if (projects.length == 1) render(projects.head)
        else render(projects, total)
      case Layout.Multi => render(projects, total)
      case Layout.Total => render(total)
    }

  def render(projects: Seq[ProjectSummary], total: Summary): String

  def render(project: ProjectSummary): String

  def render(total: Summary): String

  def filename: String
}

object Format {
  case object GitHubFlavoredMarkdown extends Format {
    val name = "GitHub flavored markdown"

    def render(projects: Seq[ProjectSummary], total: Summary): String =
      <table>
        <thead>
          <tr>
            <th colspan="2">Project</th><th colspan="4">Statements</th><th colspan="3">Branches</th>
          </tr>
          <tr>
            <th>Name</th><th>ID</th>
            <th>Total</th><th>Invoked</th><th>Ignored</th><th>Rate</th>
            <th>Total</th><th>Invoked</th><th>Rate</th>
          </tr>
        </thead>
        <tbody>
          {
        projects map { project =>
          import project.summary.metrics as m
          <tr>
            <td>{project.name}</td><td>{project.id}</td>
            <td align="right">{m.statements}</td>
            <td align="right">{m.invokedStatements}</td>
            <td align="right">{m.ignoredStatements}</td>
            <td align="right">{renderRate(project.summary.statements)(m.invokedStatements, m.statements)}</td>
            <td align="right">{m.branches}</td>
            <td align="right">{m.invokedBranches}</td>
            <td align="right">{renderRate(project.summary.branches)(m.invokedBranches, m.branches)}</td>
          </tr>
        }
      }
        </tbody>
        <tfoot align="right">
          <tr>
            <td colspan="2"></td>
            <td align="right">{total.metrics.statements}</td>
            <td align="right">{total.metrics.invokedStatements}</td>
            <td align="right">{total.metrics.ignoredStatements}</td>
            <td align="right">
              {renderRate(total.statements)(total.metrics.invokedStatements, total.metrics.statements)}
            </td>
            <td align="right">{total.metrics.branches}</td>
            <td align="right">{total.metrics.invokedBranches}</td>
            <td align="right">
              {renderRate(total.branches)(total.metrics.invokedBranches, total.metrics.branches)}
            </td>
            </tr>
          </tfoot>
      </table>.toString()

    def render(project: ProjectSummary): String =
      <table>
        <thead>
          <tr><th rowspan="2">Project</th><th>Name</th><td align="center">{project.name}</td></tr>
          <tr><th>ID</th><td align="center">{project.id}</td></tr>
        </thead>
        {renderMetricsBody(project.summary)}
      </table>.toString()

    def render(total: Summary): String = <table>{renderMetricsBody(total)}</table>.toString()

    private def renderMetricsBody(summary: Summary): Elem = {
      import summary.metrics as m
      <tbody>
        <tr><th rowspan="4">Statements</th><th>Total</th><td align="right">{m.statements}</td></tr>
        <tr><th>Invoked</th><td align="right">{m.invokedStatements}</td></tr>
        <tr><th>Ignored</th><td align="right">{m.ignoredStatements}</td></tr>
        <tr><th>Rate</th><td align="right">{renderRate(summary.statements)(m.invokedStatements, m.statements)}</td></tr>
        <tr><th rowspan="3">Branches</th><th>Total</th><td align="right">{m.branches}</td></tr>
        <tr><th>Invoked</th><td align="right">{m.invokedBranches}</td></tr>
        <tr><th>Rate</th><td align="right">{renderRate(summary.branches)(m.invokedBranches, m.branches)}</td></tr>
      </tbody>
    }

    private def renderRate(thresholds: Thresholds)(invoked: Int, total: Int): String =
      if (total == 0) "$\\textemdash$"
      else {
        val rate = invoked.toFloat / total * 100
        val color =
          if (rate < thresholds.low) "#f00"
          else if (rate < thresholds.high) "#ff0"
          else "#0f0"
        f"$$\\color{$color}$rate%2.02f\\%%$$"
      }

    override val filename: String = "gfm.md"
  }
}
