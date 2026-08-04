#!/bin/bash

set -euxo pipefail

sbt "cleanFull; +test; ci-release"
