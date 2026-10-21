#!/bin/bash

set -euxo pipefail

sbt "scalafmtSbtCheck; scalafmtCheckAll; cleanFull; +coverage; +test; +coverageSummary; +coverageAggregate; +coverageSummaryCheck"

# scripted publishes the plugin locally and runs real builds against it, so instrumentation has to be off first —
# the sbt server outlives a single invocation and would otherwise carry `+coverage` over into the published artifact.
sbt "coverageOff; +scripted"
