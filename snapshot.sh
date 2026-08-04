#!/bin/bash

set -euxo pipefail

sbt "cleanFull; +compile; ci-release"
