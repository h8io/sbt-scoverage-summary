#!/bin/bash

set -euxo pipefail

if [[ -n "${CI:-}" ]]; then
  COVERAGE_REPORT=coverageSummary
else
  COVERAGE_REPORT=coverageAggregate
fi

sbt scalafmtSbtCheck scalafmtCheckAll +clean +coverage +test +$COVERAGE_REPORT
