#!/bin/bash
# Script to validate JaCoCo coverage report existence and print summary

REPORT_PATH="target/site/jacoco/index.html"

if [ -f "$REPORT_PATH" ]; then
  echo "[OK] JaCoCo coverage report found at $REPORT_PATH"
  # Optionally, open the report in the default browser
  xdg-open "$REPORT_PATH" 2>/dev/null || open "$REPORT_PATH" 2>/dev/null || echo "Open the report manually."
else
  echo "[ERROR] JaCoCo coverage report not found. Run the E2E tests first."
  exit 1
fi
