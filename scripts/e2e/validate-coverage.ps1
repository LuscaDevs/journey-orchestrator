# PowerShell script to validate JaCoCo coverage report existence and print summary

$reportPath = "target/site/jacoco/index.html"

if (Test-Path $reportPath) {
    Write-Host "[OK] JaCoCo coverage report found at $reportPath"
    # Optionally, open the report in the default browser
    Start-Process $reportPath
} else {
    Write-Host "[ERROR] JaCoCo coverage report not found. Run the E2E tests first."
    exit 1
}
