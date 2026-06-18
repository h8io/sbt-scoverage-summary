#!/bin/bash

set -euxo pipefail

sbt "scalafmtSbtCheck; scalafmtCheckAll; cleanFull; +coverage; +test; +coverageSummary; +coverageAggregate"
