#!/bin/bash

set -euxo pipefail

# sbt scalafmtSbtCheck scalafmtCheckAll +clean +coverage +test +coverageSummary +coverageAggregate
sbt scalafmtSbtCheck scalafmtCheckAll +clean +test