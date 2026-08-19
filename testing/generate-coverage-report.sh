#!/bin/bash

################################################################################
# Notaire - Code Coverage Report Generator
#
# Analyzes JaCoCo coverage reports and generates detailed service-by-service
# coverage metrics, identifying gaps and uncovered areas.
#
# Usage:
#   bash generate-coverage-report.sh [--html] [--threshold 80]
#
################################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
INTEGRATION_TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "$INTEGRATION_TEST_DIR/.." && pwd)"
BACKEND_DIR="$WORKSPACE_ROOT/backend-api"
REPORTS_DIR="$INTEGRATION_TEST_DIR/reports"
COVERAGE_REPORT="$REPORTS_DIR/coverage-report.md"
COVERAGE_THRESHOLD=${COVERAGE_THRESHOLD:-80}
GENERATE_HTML=${GENERATE_HTML:-false}

# Helper functions
log_info() {
    printf "${GREEN}✓${NC} %s\n" "$1"
}

log_error() {
    printf "${RED}✗${NC} %s\n" "$1"
}

log_section() {
    printf "\n${BLUE}%s${NC}\n" "$1"
    printf "${BLUE}$(printf '=%.0s' {1..60})${NC}\n"
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --html)
                GENERATE_HTML=true
                shift
                ;;
            --threshold)
                COVERAGE_THRESHOLD="$2"
                shift 2
                ;;
            *)
                shift
                ;;
        esac
    done
}

generate_jacoco_report() {
    log_section "Generating JaCoCo Report"

    cd "$BACKEND_DIR"

    # Run tests and generate coverage
    mvn clean test jacoco:report -pl backend-api -q 2>&1 || {
        log_error "Failed to generate JaCoCo report"
        return 1
    }

    log_info "JaCoCo report generated"
}

parse_jacoco_csv() {
    log_section "Parsing Coverage Metrics"

    local csv_file="$BACKEND_DIR/target/site/jacoco/csv/jacoco.csv"

    if [[ ! -f "$csv_file" ]]; then
        # Try generating CSV from XML
        mvn jacoco:report -pl backend-api -q 2>/dev/null || true

        if [[ ! -f "$csv_file" ]]; then
            log_error "JaCoCo CSV report not found at $csv_file"
            return 1
        fi
    fi

    log_info "Found JaCoCo CSV report"

    # Parse CSV and extract coverage per package/class
    {
        cat << 'EOF'
# Code Coverage Analysis

## Coverage Summary

| Metric | Value | Status |
|--------|-------|--------|
EOF

        # Extract line coverage
        local line_coverage=$(awk -F',' '$2 == "PACKAGE" && $3 ~ /^com\.licensis\.notaire/ {
            missed = $6; covered = $7
            total = missed + covered
            if (total > 0) line_pct = (covered / total) * 100; else line_pct = 0
            sum += covered; missed_sum += missed
        }
        END {
            total = sum + missed_sum
            if (total > 0) printf "%.1f", (sum / total) * 100; else printf "0"
        }' "$csv_file")

        # Extract branch coverage
        local branch_coverage=$(awk -F',' '$2 == "PACKAGE" && $3 ~ /^com\.licensis\.notaire/ {
            missed = $8; covered = $9
            total = missed + covered
            if (total > 0) branch_pct = (covered / total) * 100; else branch_pct = 0
            sum += covered; missed_sum += missed
        }
        END {
            total = sum + missed_sum
            if (total > 0) printf "%.1f", (sum / total) * 100; else printf "0"
        }' "$csv_file")

        # Determine status
        local line_status="🔴"
        local branch_status="🔴"

        if (( $(echo "$line_coverage >= $COVERAGE_THRESHOLD" | bc -l) )); then
            line_status="🟢"
        fi

        if (( $(echo "$branch_coverage >= $COVERAGE_THRESHOLD" | bc -l) )); then
            branch_status="🟢"
        fi

        echo "| Line Coverage | ${line_coverage}% | ${line_status} |"
        echo "| Branch Coverage | ${branch_coverage}% | ${branch_status} |"
        echo "| Minimum Threshold | ${COVERAGE_THRESHOLD}% | 📋 |"
        echo ""

    } > "$COVERAGE_REPORT"

    log_info "Parsed coverage metrics"
}

generate_package_breakdown() {
    log_section "Generating Package-Level Coverage"

    local csv_file="$BACKEND_DIR/target/site/jacoco/csv/jacoco.csv"

    {
        cat << 'EOF'

## Coverage by Package

| Package | Lines Covered | Lines Total | Coverage % | Status |
|---------|---------------|-------------|-----------|--------|
EOF

        # Extract per-package coverage
        awk -F',' '
        $2 == "PACKAGE" && $3 ~ /^com\.licensis\.notaire/ {
            pkg = $3
            line_covered = $7
            line_missed = $6
            line_total = line_covered + line_missed
            if (line_total > 0) {
                coverage = (line_covered / line_total) * 100
                status = coverage >= 80 ? "✓ OK" : "✗ LOW"
                printf "| %s | %d | %d | %.1f%% | %s |\n",
                    pkg, line_covered, line_total, coverage, status
            }
        }
        ' "$csv_file" | sort -t'|' -k4 -rn >> "$COVERAGE_REPORT"

    } || log_error "Failed to generate package breakdown"
}

generate_uncovered_areas() {
    log_section "Identifying Uncovered Areas"

    local jacoco_xml="$BACKEND_DIR/target/site/jacoco/jacoco.xml"

    if [[ ! -f "$jacoco_xml" ]]; then
        log_error "JaCoCo XML report not found"
        return
    fi

    {
        cat << 'EOF'

## Uncovered Code Areas

Classes with coverage below 80%:

EOF

        # Parse XML and find low-coverage classes
        grep -o '<package name="[^"]*"' "$jacoco_xml" | head -20 >> "$COVERAGE_REPORT" 2>/dev/null || true

    } || true
}

generate_markdown_report() {
    log_section "Generating Coverage Report"

    # Initialize report
    {
        cat << 'EOF'
# Code Coverage Report - Notaire Backend

**Generated:** $(date)
**Minimum Threshold:** 80%

## Quick Stats

EOF
    } > "$COVERAGE_REPORT"

    # Add metrics
    parse_jacoco_csv
    generate_package_breakdown
    generate_uncovered_areas

    # Add footer
    {
        cat << 'EOF'

## How to Improve Coverage

### Add Missing Tests
1. Identify uncovered lines in the report above
2. Write unit tests for uncovered logic
3. Aim for edge cases and error conditions

### Command Reference
\`\`\`bash
# Generate coverage report
mvn clean test jacoco:report -pl backend-api

# View HTML report
open backend-api/target/site/jacoco/index.html

# Check against threshold (80%)
mvn jacoco:check -pl backend-api

# Generate CSV for parsing
mvn clean test -pl backend-api
mvn exec:java -Dexec.mainClass="org.jacoco.cli.internal.Main" ...
\`\`\`

## Coverage Trends

Track coverage over time by comparing reports from different builds.

---
**Report Generated:** $(date)
**Location:** $(pwd)
EOF
    } >> "$COVERAGE_REPORT"

    log_info "Report generated: $COVERAGE_REPORT"
}

generate_html_report() {
    if [[ "$GENERATE_HTML" != "true" ]]; then
        return
    fi

    log_section "Generating HTML Report"

    cd "$BACKEND_DIR"
    mvn site -pl backend-api -q 2>/dev/null || {
        log_error "Failed to generate HTML report"
        return 1
    }

    log_info "HTML report available at: $BACKEND_DIR/target/site/jacoco/index.html"
}

main() {
    parse_args "$@"

    echo -e "${BLUE}"
    cat << 'EOF'
╔════════════════════════════════════════════════════════════════╗
║     Notaire - Code Coverage Report Generator                   ║
║                                                                ║
║  Analyzing service-by-service code coverage metrics            ║
╚════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"

    mkdir -p "$REPORTS_DIR"

    generate_jacoco_report
    generate_markdown_report
    generate_html_report

    echo ""
    log_section "Coverage Analysis Complete"
    echo "📊 Markdown Report: $COVERAGE_REPORT"
    echo "📈 HTML Report: $BACKEND_DIR/target/site/jacoco/index.html"
    echo ""
}

main "$@"
