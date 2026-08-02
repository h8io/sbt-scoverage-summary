# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An sbt plugin (`io.h8.sbt:sbt-scoverage-summary`) that renders scoverage results as a compact summary file,
primarily so `h8io/gha` can post it as a pull request comment. It is a sibling to `../gha`, which consumes its output.

## Commands

```bash
./test.sh                  # exactly what CI runs: scalafmt checks, cleanFull, cross-build coverage/test/summary/aggregate
sbt +test                  # both cross-build rows
sbt plugin/test            # Scala 3.8.4 row only
sbt plugin2_12/test        # Scala 2.12.21 row only
sbt "plugin/testOnly h8io.sbt.scoverage.FormatTest"
sbt "plugin/testOnly h8io.sbt.scoverage.FormatTest -- -z \"substring of the test name\""
sbt scalafmtAll scalafmtSbt   # format; scalafmtCheckAll / scalafmtSbtCheck to verify
sbt +publishLocal          # both rows to ~/.ivy2/local
```

Project ids come from `sbt-projectmatrix`: `plugin` and `plugin2_12`, aggregated by `sbt-scoverage-summary-root`.
`cleanFull` in `test.sh` is sbt 2.0-only and has no sbt 1.x equivalent.

## Architecture

### Two plugins, split by trigger

`ScoverageProjectSummaryPlugin` has `trigger = allRequirements`, so it lands on every JVM project without the user
opting in. Its `summary` task (package-private) reads `crossTarget / scoverage-data` directly —
`Serializer.deserialize` plus `IOUtils.invoked(IOUtils.findMeasurementFiles(...))` — and yields
`Option[ProjectSummary]`, `None` when the data dir is absent.

`ScoverageSummaryPlugin` has `trigger = noTrigger` and is enabled by hand on the aggregating project. Its
`coverageSummary` task harvests `summary` across `inAggregates(ThisProject, includeRoot = true)` via a `ScopeFilter`,
folds the metrics into a total, and writes `crossTarget / scoverage-summary / <format.filename>`.
`coverageSummary / aggregate := false` stops sbt from additionally running it per module.

The split exists because collection must happen everywhere automatically while rendering happens once, at the root.

### It deliberately bypasses `coverageReport`

Reading raw measurement files means the summary does not depend on scoverage's report tasks and performs no
minimum-coverage check of its own. That ordering is load-bearing in `test.sh`: `+coverageSummary` runs *before*
`+coverageAggregate`, so the summary file exists on disk even when the aggregate's minimum check fails the build.
Note that scoverage's `CoverageMinimum.All.checkCoverage` short-circuits (`&&` and `forall`), so it reports only the
first violated metric — unlike this plugin, which holds every `ProjectSummary` at once.

### Rendering

`Format` (only `GitHubFlavoredMarkdown` so far) × `Layout` (`Auto` / `Multi` / `Total`). The package-private
`Format.render(layout, low, high)` dispatches to one of three public overloads — multi-project table, single-project
table, totals only. `Auto` picks by project count.

GFM output is scala-xml `Elem.toString`, and coverage rates are emitted as LaTeX (`$\color{#f00}12.34\%$`) because
GitHub renders math in comments; `total == 0` renders an em-dash rather than dividing by zero.
`coverageSummaryLowThreshold` / `coverageSummaryHighThreshold` are presentation-only — they choose a color, they do
not fail anything.

### Output path is a contract

`h8io/gha/actions/publish-scoverage-summary` globs `./**/target/**/scoverage-summary/gfm.md`, concatenates the sorted
matches, and posts them. Changing the directory name or `Format.filename` breaks that action.

## Cross-building constraints

`projectMatrix` produces two rows that must both compile:

| Row | Scala | sbt (`pluginCrossBuild / sbtVersion`) |
|---|---|---|
| `plugin` | 3.8.4 | 2.0.0 |
| `plugin2_12` | 2.12.21 | 1.8.0 |

- Both rows are warning-fatal (`-Werror` on 3, `-Xfatal-warnings -Xlint:_` on 2.12), and 2.12 compiles with
  `-Xsource:3`, so write source that satisfies the Scala 3 dialect.
- `scala-xml` is an explicit dependency for Scala 3 only — sbt 1.x already ships it.
- scalafmt is configured with `dialect: scala3` and `fatalWarnings: true`, `maxColumn: 120`.
- Task keys carry `@transient`; sbt 2.0 needs it. A past commit also had to rename a project `val` to stop the sbt 2.0
  `set` command crashing, so be wary of `val` names that collide with setting keys.

## Self-hosting caveat

`project/plugins.sbt` depends on the *released* `io.h8.sbt:sbt-scoverage-summary`, and `build.sbt` enables
`ScoverageSummaryPlugin` on the matrix. So this repo's own `coverageSummary` exercises the published version, not
your working tree — local changes only show up after `publishLocal` plus a version bump in `project/plugins.sbt`.
