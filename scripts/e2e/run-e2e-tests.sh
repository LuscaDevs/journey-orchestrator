#!/bin/bash
# Run all E2E tests with Maven Failsafe and generate coverage report

set -e

PROFILE=${1:-e2e-test}

mvn clean verify -Dspring.profiles.active=$PROFILE

REPORT_PATH="target/site/jacoco/index.html"

if [ -f "$REPORT_PATH" ]; then
  echo "JaCoCo coverage report generated at $REPORT_PATH"
else
  echo "Coverage report not found. Check Maven output for errors."
fi
