package h8io.sbt.scoverage

import scoverage.domain.Coverage

final case class Metrics(
    statements: Int,
    invokedStatements: Int,
    ignoredStatements: Int,
    branches: Int,
    invokedBranches: Int
) {
  def +(that: Metrics): Metrics = Metrics(
    statements + that.statements,
    invokedStatements + that.invokedStatements,
    ignoredStatements + that.ignoredStatements,
    branches + that.branches,
    invokedBranches + that.invokedBranches
  )
}

object Metrics {
  def apply(coverage: Coverage): Metrics = Metrics(
    coverage.statementCount,
    coverage.invokedStatementCount,
    coverage.ignoredStatementCount,
    coverage.branchCount,
    coverage.invokedBranchesCount
  )
}
