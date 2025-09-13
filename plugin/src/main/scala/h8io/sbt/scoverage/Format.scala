package h8io.sbt.scoverage

import scala.xml.Elem

trait Format {
  def name: String

  final private[scoverage] def render(layout: Layout, lowThreshold: Float, highThreshold: Float)(
      projects: Seq[ProjectSummary],
      total: Metrics
  ): String = {
    layout match {
      case Layout.Auto =>
        if (projects.length == 1) render(lowThreshold, highThreshold, projects.head)
        else render(lowThreshold, highThreshold, projects, total)
      case Layout.Multi => render(lowThreshold, highThreshold, projects, total)
      case Layout.Total => render(lowThreshold, highThreshold, total)
    }
  }

  def render(lowThreshold: Float, highThreshold: Float, projects: Seq[ProjectSummary], total: Metrics): String

  def render(lowThreshold: Float, highThreshold: Float, project: ProjectSummary): String

  def render(lowThreshold: Float, highThreshold: Float, total: Metrics): String

  def filename: String
}

object Format {
  case object GitHubFlavoredMarkdown extends Format {
    val name = "GitHub flavored markdown"

    def render(lowThreshold: Float, highThreshold: Float, projects: Seq[ProjectSummary], total: Metrics): String = {
      val valueRender = render(lowThreshold, highThreshold) _
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
          import project.metrics as m
          <tr>
            <td>{project.name}</td><td>{project.id}</td>
            <td align="right">{m.statements}</td>
            <td align="right">{m.invokedStatements}</td>
            <td align="right">{m.ignoredStatements}</td>
            <td align="right">{
            if (m.statements == 0) "" else valueRender(m.invokedStatements, m.statements)
          }</td>
            <td align="right">{m.branches}</td>
            <td align="right">{m.invokedBranches}</td>
            <td align="right">{
            if (m.branches == 0) "" else valueRender(m.invokedBranches, m.branches)
          }</td>
          </tr>
        }
      }
        </tbody>
        <tfoot align="right">
          <tr>
            <td colspan="2"></td>
            <td>{total.statements}</td>
            <td>{total.invokedStatements}</td>
            <td>{total.ignoredStatements}</td>
            <td>{if (total.statements == 0) "" else valueRender(total.invokedStatements, total.statements)}</td>
            <td>{total.branches}</td>
            <td>{total.invokedBranches}</td>
            <td>{if (total.branches == 0) "" else valueRender(total.invokedBranches, total.branches)}</td>
            </tr>
          </tfoot>
      </table>.toString()
    }

    def render(lowThreshold: Float, highThreshold: Float, project: ProjectSummary): String =
      <table>
        <thead>
          <tr><th rowspan="2">Project</th><th>Name</th><td>{project.name}</td></tr>
          <tr><th>ID</th><td>{project.id}</td></tr>
        </thead>
        {renderMetricsBody(lowThreshold, highThreshold, project.metrics)}
      </table>.toString()

    def render(lowThreshold: Float, highThreshold: Float, metrics: Metrics): String =
      <table>{renderMetricsBody(lowThreshold, highThreshold, metrics)}</table>.toString()

    private def renderMetricsBody(lowThreshold: Float, highThreshold: Float, metrics: Metrics): Elem = {
      val valueRender = render(lowThreshold, highThreshold) _
      <tbody>
        <tr><th rowspan="4">Statements</th><th>Total</th><td>{metrics.statements}</td></tr>
        <tr><th>Invoked</th><td>{metrics.invokedStatements}</td></tr>
        <tr><th>Ignored</th><td>{metrics.ignoredStatements}</td></tr>
        <tr><th>Rate</th><td>{valueRender(metrics.invokedStatements, metrics.statements)}</td></tr>
        <tr><th rowspan="3">Branches</th><th>Total</th><td>{metrics.branches}</td></tr>
        <tr><th>Invoked</th><td>{metrics.invokedBranches}</td></tr>
        <tr><th>Rate</th><td>{valueRender(metrics.invokedBranches, metrics.branches)}</td></tr>
      </tbody>
    }

    private def render(lowThreshold: Float, highThreshold: Float)(invoked: Int, total: Int): String = {
      val rate = invoked.toFloat / total * 100
      val color =
        if (rate <= lowThreshold) "#f00"
        else if (rate < highThreshold) "#ff0"
        else "#0f0"
      f"$$\\color{$color}$rate%2.02f\\%%$$"
    }

    override val filename: String = "gfm.md"
  }
}
