# About

This plugin generates and publishes scoverage summary reports for SBT projects.
Summary report can be published as a GitHub comment in a pull request.
This project uses it itself, and could be used as a reference for usage
(as well as other H8IO projects).

## Settings

* coverageSummaryFormat - type is
  [Format](https://github.com/h8io/sbt-scoverage-summary/blob/master/plugin/src/main/scala/h8io/sbt/scoverage/Format.scala),
  at this moment only one value is implemented:
  `GitHubFlavoredMarkdown` (default),
  but there is no limitations to implement it in other projects.
* coverageSummaryLayout - type is
  [Layout](https://github.com/h8io/sbt-scoverage-summary/blob/master/plugin/src/main/scala/h8io/sbt/scoverage/Layout.scala),
  values are
    * Auto - choose a layout depends on number of tested modules.
      There are two layouts: for a single-module projects,
      another one for multimodule projects.
    * Multi - always show multimodule project layout.
    * Total - show only total summary value.
* coverageSummaryStmtLowThreshold, coverageSummaryBranchLowThreshold - type is Float,
  this value is used to color in red when the corresponding coverage rate
  is lesser than this value.
* coverageSummaryStmtHighThreshold, coverageSummaryBranchHighThreshold - type is Float,
  this value is used to color in green when the corresponding coverage rate
  is greater or equal than this value.

  Statement and branch coverage are separate metrics whose rates are not correlated,
  so each of them has its own pair of thresholds.

  Within a metric the thresholds must satisfy `0 <= low <= high <= 100`,
  otherwise `coverageSummary` fails. Equal thresholds are allowed
  and produce a two-color scale with no intermediate color.

### Per-module thresholds

Thresholds are resolved separately for every module, so each row of the report
can be judged by its own values. There is nothing to enable: this is the regular
SBT `project -> ThisBuild -> Global` delegation, and the defaults above live in
the global scope.

```sbt
// applies to every module which does not say otherwise
ThisBuild / coverageSummaryStmtLowThreshold := 60

// this one is held to a higher standard
lazy val core = project.settings(coverageSummaryStmtLowThreshold := 90)

// and this one is not expected to be covered at all
lazy val examples = project.settings(coverageSummaryStmtLowThreshold := 0)
```

The keys can be scoped to any module, including modules where
`ScoverageSummaryPlugin` itself is not enabled.

Every module is validated, and a report which is inconsistent in several modules
lists all of them at once, each prefixed with the module id.

The total row is judged by the thresholds of the aggregating project. Its color
is therefore not derivable from the colors of the rows above it: modules that are
all green can still add up to a yellow total, since the total is a different
number measured against a different pair of thresholds.

## Usage

### plugins.sbt

Add

```sbt
addSbtPlugin("io.h8.sbt" % "sbt-scoverage-summary" % "x.x.x")
```
[![GitHub release](https://img.shields.io/github/v/release/h8io/sbt-scoverage-summary)](https://github.com/h8io/sbt-scoverage-summary/releases/latest)

### build.sbt

In the root project enable `ScoverageSummaryPlugin`

### .github/workflow/test.yaml

A full example of usage could be found
[here](https://github.com/h8io/gha/blob/master/.github/workflows/test.yaml).

#### Add the step for summary creation:

```yaml
- name: Create scoverage summary
  run: sbt clean +coverage +test +coverageSummary
```

#### Add the step for publishing

```yaml
- name: Publish scoverage summary
  uses: h8io/gha/actions/publish-scoverage-summary@v2
  with:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```