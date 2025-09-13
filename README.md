# Usage

This plugin generates and publish scoverage summary reports for SBT projects.
Summary report could be published as a GitHub comment in a pull request.
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
    There are two layouts: one for a single-module projects,
    another one for multimodule projects.
  * Multi - always show multimodule project layout.
  * Total - show only total summary value.
* coverageSummaryLowThreshold - type is Float, this value is used
  to color in red when coverage rate is lesser or equal than this value.
* coverageSummaryHighThreshold - type is Float, this value is used
  to color in green when coverage rate is greater or equal than this value.

## plugins.sbt

```sbt
addSbtPlugin("io.h8.sbt" % "sbt-scoverage-summary" % "x.x.x")
```

## build.sbt

In the root project enable `ScoverageSummaryPlugin`

## .github/workflow/test.yaml

A full example uf usage could be found
[here](https://github.com/h8io/.github/blob/master/.github/workflows/test.yaml).

### Add the step for summary creation:

```yaml
- name: Create scoverage summary
  run: sbt clean +coverage +test +coverageSummary
```

### Add the step for publishing

```yaml
- name: Publish scoverage summary
  if: ${{ github.event.pull_request }}
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  run: |
    set -euo pipefail
    shopt -s globstar
    if [[ ${{ inputs.is-plugin }} == "true" ]]; then
      TARGET=plugin/target
    else
      TARGET=target
    fi
    SUMMARY_FILE=scoverage-summary.md
    echo "# ${{ github.event.pull_request.head.sha }}" > $SUMMARY_FILE
    cat $(ls ./$TARGET/**/scoverage-summary/gfm.md | sort) >> $SUMMARY_FILE
    gh pr comment "${{ github.event.pull_request.number }}" --body-file $SUMMARY_FILE
```