package h8io.sbt.scoverage

trait Format {
  def render(projects: Seq[ProjectSummary], total: Metrics, lowThreshold: Float, highThreshold: Float): String

  def fileName: String
}

object Format {
  case object GitHubFlavoredMarkdown extends Format {
    def render(projects: Seq[ProjectSummary], total: Metrics, lowThreshold: Float, highThreshold: Float): String = {
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
            <td>{project.name}</td><td>{project.ref.project}</td>
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

    private def render(lowThreshold: Float, highThreshold: Float)(invoked: Int, total: Int): String = {
      val rate = invoked.toFloat / total * 100
      val color =
        if (rate <= lowThreshold) "#f00"
        else if (rate < highThreshold) "#ff0"
        else "#0f0"
      f"$$\\color{$color}$rate%2.02f\\%%$$"
    }

    override def fileName: String = "gfm.md"
  }
}
