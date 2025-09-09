package h8io.sbt.scoverage

trait Format {
  def render(projects: Seq[ProjectSummary], total: Metrics): String

  def fileSuffix: String
}

object Format {
  case object GitHubFlavoredMarkdown extends Format {
    def render(projects: Seq[ProjectSummary], total: Metrics): String = {
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
            if (m.statements == 0) "" else f"${m.invokedStatements.toDouble / m.statements * 100}%2.02f%%"
          }</td>
            <td align="right">{m.branches}</td>
            <td align="right">{m.invokedBranches}</td>
            <td align="right">{
            if (m.branches == 0) "" else f"${m.invokedBranches.toDouble / m.branches * 100}%2.02f%%"
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
            <td>{
        if (total.statements == 0) "" else f"${total.invokedStatements.toDouble / total.statements * 100}%2.02f%%"
      }</td>
            <td>{total.branches}</td>
            <td>{total.invokedBranches}</td>
            <td>{
        if (total.branches == 0) "" else f"${total.invokedBranches.toDouble / total.branches * 100}%2.02f%%"
      }</td>
            </tr>
          </tfoot>
      </table>
    }.toString()

    override def fileSuffix: String = "-gfm.html"
  }
}
