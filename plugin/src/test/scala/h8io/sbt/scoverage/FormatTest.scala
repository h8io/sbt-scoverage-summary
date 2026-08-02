package h8io.sbt.scoverage

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FormatTest extends AnyFlatSpec with Matchers with MockFactory {
  private val noMinimum = Minimum(0, 0)
  private val statements = Thresholds(0.3f, 0.8f)
  private val branches = Thresholds(0.1f, 0.5f)

  private def summary(metrics: Metrics) = Summary(metrics, statements, branches, noMinimum)

  private val project = ProjectSummary("root", "single", summary(Metrics(12, 7, 1, 10, 5)))
  private val projects = Seq(
    ProjectSummary("project1", "project-1", summary(Metrics(23, 21, 3, 11, 10))),
    ProjectSummary("project2", "project-2", summary(Metrics(17, 11, 0, 19, 13)))
  )
  private val total = summary(projects.iterator.map(_.summary.metrics).reduce(_ + _))

  "render" should s"invoke the correct method for a single project when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "single project summary with auto layout"
    (format.render(_: ProjectSummary)).expects(project).returns(result)
    format.render(Layout.Auto)(project :: Nil, project.summary) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "multiproject summary with auto layout"
    (format.render(_: Seq[ProjectSummary], _: Summary)).expects(projects, total).returns(result)
    format.render(Layout.Auto)(projects, total) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Multi}"
    (format.render(_: Seq[ProjectSummary], _: Summary)).expects(project :: Nil, project.summary).returns(result)
    format.render(Layout.Multi)(project :: Nil, project.summary) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Multi}"
    (format.render(_: Seq[ProjectSummary], _: Summary)).expects(projects, total).returns(result)
    format.render(Layout.Multi)(projects, total) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Total}"
    (format.render(_: Summary)).expects(project.summary).returns(result)
    format.render(Layout.Total)(project :: Nil, project.summary) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Total}"
    (format.render(_: Summary)).expects(total).returns(result)
    format.render(Layout.Total)(projects, total) shouldEqual result
  }

  s"${Format.GitHubFlavoredMarkdown.name}" should "color statements and branches by their own thresholds" in {
    val equalRates = Summary(Metrics(10, 5, 0, 10, 5), Thresholds(60, 80), Thresholds(20, 40), noMinimum)
    val rendered = Format.GitHubFlavoredMarkdown.render(equalRates)
    rendered should include("\\color{#f00}50.00")
    rendered should include("\\color{#0f0}50.00")
  }

  it should "color every project by its own thresholds" in {
    val metrics = Metrics(10, 5, 0, 10, 5)
    val strict = ProjectSummary("strict", "strict", Summary(metrics, Thresholds(60, 80), Thresholds(60, 80), noMinimum))
    val lenient =
      ProjectSummary("lenient", "lenient", Summary(metrics, Thresholds(20, 40), Thresholds(20, 40), noMinimum))
    val rendered = Format.GitHubFlavoredMarkdown
      .render(Seq(strict, lenient), Summary(metrics + metrics, Thresholds(40, 60), Thresholds(40, 60), noMinimum))
    rendered should include("\\color{#f00}50.00")
    rendered should include("\\color{#0f0}50.00")
    rendered should include("\\color{#ff0}50.00")
  }
}
