package h8io.sbt.scoverage

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FormatTest extends AnyFlatSpec with Matchers with MockFactory {
  private val lowThreshold: Float = 0.3f
  private val highThreshold: Float = 0.8f

  private val project = ProjectSummary("root", "single", Metrics(12, 7, 1, 10, 5))
  private val projects = Seq(
    ProjectSummary("project1", "project-1", Metrics(23, 21, 3, 11, 10)),
    ProjectSummary("project2", "project-2", Metrics(17, 11, 0, 19, 13))
  )
  private val metrics = projects.iterator.map(_.metrics).reduce(_ + _)

  classOf[Format].getName should s"invoke the correct method for a single project when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "single project summary with auto layout"
    (format.render(_: Float, _: Float, _: ProjectSummary)).expects(lowThreshold, highThreshold, project).returns(result)
    format.render(Layout.Auto, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Auto}" in {
    val format = mock[Format]
    val result = "multiproject summary with auto layout"
    (format
      .render(_: Float, _: Float, _: Seq[ProjectSummary], _: Metrics))
      .expects(lowThreshold, highThreshold, projects, metrics)
      .returns(result)
    format.render(Layout.Auto, lowThreshold, highThreshold)(projects, metrics) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Multi}"
    (format
      .render(_: Float, _: Float, _: Seq[ProjectSummary], _: Metrics))
      .expects(lowThreshold, highThreshold, project :: Nil, project.metrics)
      .returns(result)
    format.render(Layout.Multi, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Multi}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Multi}"
    (format
      .render(_: Float, _: Float, _: Seq[ProjectSummary], _: Metrics))
      .expects(lowThreshold, highThreshold, projects, metrics)
      .returns(result)
    format.render(Layout.Multi, lowThreshold, highThreshold)(projects, metrics) shouldEqual result
  }

  it should s"invoke the correct method for a single project when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"single project summary with layout ${Layout.Total}"
    (format
      .render(_: Float, _: Float, _: Metrics))
      .expects(lowThreshold, highThreshold, project.metrics)
      .returns(result)
    format.render(Layout.Total, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual result
  }

  it should s"invoke the correct method for multiple projects when layout is ${Layout.Total}" in {
    val format = mock[Format]
    val result = s"multiproject summary with layout ${Layout.Total}"
    (format
      .render(_: Float, _: Float, _: Metrics))
      .expects(lowThreshold, highThreshold, metrics)
      .returns(result)
    format.render(Layout.Total, lowThreshold, highThreshold)(projects, metrics) shouldEqual result
  }
}
