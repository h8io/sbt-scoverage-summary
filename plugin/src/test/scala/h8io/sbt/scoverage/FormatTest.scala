package h8io.sbt.scoverage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FormatTest extends AnyFlatSpec with Matchers {
  private val lowThreshold: Float = 0.3f
  private val highThreshold: Float = 0.8f

  private val project = ProjectSummary("root", "single", Metrics(12, 7, 1, 10, 5))
  private val projects = Seq(
    ProjectSummary("project1", "project-1", Metrics(23, 21, 3, 11, 10)),
    ProjectSummary("project2", "project-2", Metrics(17, 11, 0, 19, 13))
  )
  private val metrics = projects.iterator.map(_.metrics).reduce(_ + _)

  private sealed trait Call
  private case class MultiCall(projects: Seq[ProjectSummary], total: Metrics) extends Call
  private case class SingleCall(project: ProjectSummary) extends Call
  private case class TotalCall(metrics: Metrics) extends Call

  private def trackingFormat(): (Format, () => Option[Call]) = {
    var lastCall: Option[Call] = None
    val format = new Format {
      def render(low: Float, high: Float, ps: Seq[ProjectSummary], total: Metrics): String = {
        lastCall = Some(MultiCall(ps, total)); "multi"
      }
      def render(low: Float, high: Float, p: ProjectSummary): String = { lastCall = Some(SingleCall(p)); "single" }
      def render(low: Float, high: Float, m: Metrics): String = { lastCall = Some(TotalCall(m)); "total" }
      val name: String = "test"
      val filename: String = "test.md"
    }
    (format, () => lastCall)
  }

  "render" should s"invoke single-project render for a single project when layout is ${Layout.Auto}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Auto, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual "single"
    getCall() shouldEqual Some(SingleCall(project))
  }

  it should s"invoke multi-project render for multiple projects when layout is ${Layout.Auto}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Auto, lowThreshold, highThreshold)(projects, metrics) shouldEqual "multi"
    getCall() shouldEqual Some(MultiCall(projects, metrics))
  }

  it should s"invoke multi-project render for a single project when layout is ${Layout.Multi}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Multi, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual "multi"
    getCall() shouldEqual Some(MultiCall(project :: Nil, project.metrics))
  }

  it should s"invoke multi-project render for multiple projects when layout is ${Layout.Multi}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Multi, lowThreshold, highThreshold)(projects, metrics) shouldEqual "multi"
    getCall() shouldEqual Some(MultiCall(projects, metrics))
  }

  it should s"invoke total render for a single project when layout is ${Layout.Total}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Total, lowThreshold, highThreshold)(project :: Nil, project.metrics) shouldEqual "total"
    getCall() shouldEqual Some(TotalCall(project.metrics))
  }

  it should s"invoke total render for multiple projects when layout is ${Layout.Total}" in {
    val (format, getCall) = trackingFormat()
    format.render(Layout.Total, lowThreshold, highThreshold)(projects, metrics) shouldEqual "total"
    getCall() shouldEqual Some(TotalCall(metrics))
  }
}
