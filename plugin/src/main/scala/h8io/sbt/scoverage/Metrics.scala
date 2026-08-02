package h8io.sbt.scoverage

import scoverage.domain.Coverage

final case class Metrics(
    statements: Int,
    invokedStatements: Int,
    ignoredStatements: Int,
    branches: Int,
    invokedBranches: Int
) {
  def +(that: Metrics): Metrics =
    Metrics(
      statements + that.statements,
      invokedStatements + that.invokedStatements,
      ignoredStatements + that.ignoredStatements,
      branches + that.branches,
      invokedBranches + that.invokedBranches
    )

  /** Percentages of the covered code, `None` when there is nothing to cover. Both the report and the coverage check
    * read the rates from here, so that a cell rendered in red and a failing module can never disagree.
    */
  def statementRate: Option[Float] = Metrics.rate(invokedStatements, statements)

  def branchRate: Option[Float] = Metrics.rate(invokedBranches, branches)
}

object Metrics {
  // Dividing equal Ints is exact in IEEE-754, so full coverage yields exactly 100 and a minimum of 100 is reachable.
  private def rate(invoked: Int, total: Int): Option[Float] =
    if (total == 0) None else Some(invoked.toFloat / total * 100)

  def apply(coverage: Coverage): Metrics =
    Metrics(
      coverage.statementCount,
      coverage.invokedStatementCount,
      coverage.ignoredStatementCount,
      coverage.branchCount,
      coverage.invokedBranchesCount
    )
}
