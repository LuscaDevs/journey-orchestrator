# PowerShell script to run all E2E tests and generate JaCoCo coverage report

param(
    [string]$Profile = "e2e-test"
)

mvn clean verify -Dspring.profiles.active=$Profile

$reportPath = "target/site/jacoco/index.html"
if (Test-Path $reportPath) {
    Write-Host "JaCoCo coverage report generated at $reportPath"
} else {
    Write-Host "Coverage report not found. Check Maven output for errors."
}
