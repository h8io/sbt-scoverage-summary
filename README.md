# About

This plugin generates and publishes scoverage summary reports for SBT projects.
Summary report can be published as a GitHub comment in a pull request.
This project uses it itself, and could be used as a reference for usage
(as well as other H8IO projects).

⚠️ SBT 1.8.0 or newer required.

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
* coverageSummaryLowThreshold - type is Float, this value is used
  to color in red when coverage rate is lesser or equal than this value.
* coverageSummaryHighThreshold - type is Float, this value is used
  to color in green when coverage rate is greater or equal than this value.

## Usage

### plugins.sbt

Add

```sbt
addSbtPlugin("io.h8.sbt" % "sbt-scoverage-summary" % "x.x.x")
```

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