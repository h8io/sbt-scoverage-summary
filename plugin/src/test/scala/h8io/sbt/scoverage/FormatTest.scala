package h8io.sbt.scoverage

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FormatTest extends AnyFlatSpec with Matchers with MockFactory {
  private val statements = Thresholds(0.3f, 0.8f)
  private val branches = Thresholds(0.1f, 0.5f)

  private val project = ProjectSummary("root", "single", Metrics(12, 7, 1, 10, 5))
  private val projects = Seq(
    ProjectSummary("project1", "project-1", Metrics(23, 21, 3, 11, 10)),
    ProjectSummary("project2", "project-2", Metrics(17, 11, 0, 19, 13))
  )
  private val metrics = projects.iterator.map(_.metrics).reduce(_ + _)

  "render" should s"invoke the correct method for a single project when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "single project summary with auto layout"
    (format
      .render(_: Thresholds, _: Thresholds, _: ProjectSummary))
      .expects(statements, branches, project)
      .returns(result)
    format.render(Layout.Auto, statements, branches)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "multiproject summary with auto layout"
    (format
      .render(_: Thresholds, _: Thresholds, _: Seq[ProjectSummary], _: Metrics))
      .expects(statements, branches, projects, metrics)
      .returns(result)
    format.render(Layout.Auto, statements, branches)(projects, metrics) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Multi}"
    (format
      .render(_: Thresholds, _: Thresholds, _: Seq[ProjectSummary], _: Metrics))
      .expects(statements, branches, project :: Nil, project.metrics)
      .returns(result)
    format.render(Layout.Multi, statements, branches)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Multi}"
    (format
      .render(_: Thresholds, _: Thresholds, _: Seq[ProjectSummary], _: Metrics))
      .expects(statements, branches, projects, metrics)
      .returns(result)
    format.render(Layout.Multi, statements, branches)(projects, metrics) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Total}"
    (format
      .render(_: Thresholds, _: Thresholds, _: Metrics))
      .expects(statements, branches, project.metrics)
      .returns(result)
    format.render(Layout.Total, statements, branches)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Total}"
    (format
      .render(_: Thresholds, _: Thresholds, _: Metrics))
      .expects(statements, branches, metrics)
      .returns(result)
    format.render(Layout.Total, statements, branches)(projects, metrics) shouldEqual result
  }

  s"${Format.GitHubFlavoredMarkdown.name}" should "color statements and branches by their own thresholds" in {
    val equalRates = Metrics(10, 5, 0, 10, 5)
    val rendered = Format.GitHubFlavoredMarkdown.render(Thresholds(60, 80), Thresholds(20, 40), equalRates)
    rendered should include("\\color{#f00}50.00")
    rendered should include("\\color{#0f0}50.00")
  }
}
