#!/bin/bash
# Generate decorative Markdown report for CI results

REPORT_FILE="reports/ci-report.md"

# Get current timestamp
TIMESTAMP=$(date -u +"%Y-%m-%d %H:%M:%S")

# Extract test results from XML files if they exist
get_test_summary() {
    local xml_file=$1
    local test_type=$2
    
    if [ -f "$xml_file" ]; then
        local total=$(grep -oP 'tests="\K[0-9]+' "$xml_file" | head -1)
        local failures=$(grep -oP 'failures="\K[0-9]+' "$xml_file" | head -1)
        local errors=$(grep -oP 'errors="\K[0-9]+' "$xml_file" | head -1)
        local skipped=$(grep -oP 'skipped="\K[0-9]+' "$xml_file" | head -1)
        
        total=${total:-0}
        failures=${failures:-0}
        errors=${errors:-0}
        skipped=${skipped:-0}
        
        local success=$((total - failures - errors - skipped))
        local rate=0
        if [ "$total" -gt 0 ]; then
            rate=$((success * 100 / total))
        fi
        
        echo "| ${test_type} | ${total} | ${success} | ${failures} | ${errors} | ${skipped} | ${rate}% |"
    else
        echo "| ${test_type} | - | - | - | - | - | - |"
    fi
}

# Generate security summary
get_security_summary() {
    local json_file=$1
    
    if [ -f "$json_file" ]; then
        local critical=$(jq -r '[.Results[].Vulnerabilities[]? | select(.Severity == "CRITICAL")] | length' "$json_file" 2>/dev/null || echo 0)
        local high=$(jq -r '[.Results[].Vulnerabilities[]? | select(.Severity == "HIGH")] | length' "$json_file" 2>/dev/null || echo 0)
        local medium=$(jq -r '[.Results[].Vulnerabilities[]? | select(.Severity == "MEDIUM")] | length' "$json_file" 2>/dev/null || echo 0)
        local low=$(jq -r '[.Results[].Vulnerabilities[]? | select(.Severity == "LOW")] | length' "$json_file" 2>/dev/null || echo 0)
        
        echo "| Severity | Count |"
        echo "|----------|-------|"
        echo "| 🔴 Critical | ${critical:-0} |"
        echo "| 🟠 High | ${high:-0} |"
        echo "| 🟡 Medium | ${medium:-0} |"
        echo "| 🟢 Low | ${low:-0} |"
    else
        echo "*No vulnerabilities found*"
    fi
}

# Generate coverage summary
get_coverage_summary() {
    local xml_file=$1
    
    if [ -f "$xml_file" ]; then
        echo "| Type | Covered | Total | Percentage |"
        echo "|------|---------|-------|------------|"
        
        for type in LINE BRANCH; do
            local covered=$(grep -oP "<counter type=\"${type}\" covered=\"\K[0-9]+" "$xml_file" | head -1)
            local missed=$(grep -oP "<counter type=\"${type}\" missed=\"\K[0-9]+" "$xml_file" | head -1)
            
            covered=${covered:-0}
            missed=${missed:-0}
            total=$((covered + missed))
            
            if [ "$total" -gt 0 ]; then
                pct=$((covered * 100 / total))
                emoji=$( [ $pct -ge 80 ] && echo "✅" || [ $pct -ge 50 ] && echo "⚠️" || echo "❌" )
                echo "| ${type} | ${covered} | ${total} | ${emoji} ${pct}% |"
            else
                echo "| ${type} | 0 | 0 | - |"
            fi
        done
    else
        echo "*No coverage data available*"
    fi
}

# Check for spotbugs issues
get_quality_summary() {
    local xml_file=$1
    
    if [ -f "$xml_file" ]; then
        local total=$(grep -c 'BugInstance' "$xml_file" 2>/dev/null || echo 0)
        if [ "$total" -eq 0 ]; then
            echo "✅ **No issues found** - Code quality is excellent"
        else
            echo "⚠️ **${total} issues found** - Review required"
            echo ""
            echo "### Top Issues"
            echo "| Type | Count |"
            echo "|------|-------|"
            
            # Get top 5 bug types
            grep -oP '<BugInstance type="\K[^"]+' "$xml_file" 2>/dev/null | sort | uniq -c | sort -rn | head -5 | while read count type; do
                echo "| ${type} | ${count} |"
            done
        fi
    else
        echo "✅ **No issues found** - Code quality checks passed"
    fi
}

# Main report generation
cat > "$REPORT_FILE" << 'EOF'
---
title: CI Report
nav_order: 1
---

# 📊 CI/CD Pipeline Report

| Metadata | Value |
|----------|-------|
| **Generated** | TIMESTAMP_PLACEHOLDER |
| **Branch** | BRANCH_PLACEHOLDER |
| **Commit** | COMMIT_PLACEHOLDER |
| **Workflow** | CI Pipeline |

---

## 📋 Job Summary

EOF

# Replace placeholders
sed -i "s/TIMESTAMP_PLACEHOLDER/$TIMESTAMP/g" "$REPORT_FILE"
sed -i "s/BRANCH_PLACEHOLDER/${{ github.ref_name }}/g" "$REPORT_FILE"
sed -i "s/COMMIT_PLACEHOLDER/${{ github.sha }}/g" "$REPORT_FILE"

# Add job results table
cat >> "$REPORT_FILE" << 'EOF'

| Job | Status | Description |
|-----|--------|-------------|
| Build | JOB_BUILD | Compile and verify project |
| Unit Tests | JOB_UNIT | Run unit tests for all modules |
| Integration Tests | JOB_INTEGRATION | Run integration tests |
| Code Coverage | JOB_COVERAGE | Analyze code coverage |
| Security Scan | JOB_SECURITY | Scan for vulnerabilities |
| Docker Build | JOB_DOCKER | Build Docker image |
| Quality | JOB_QUALITY | Static code analysis |

EOF

# Replace job placeholders
sed -i "s/JOB_BUILD/${{ needs.build.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_UNIT/${{ needs.unit-tests.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_INTEGRATION/${{ needs.integration-tests.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_COVERAGE/${{ needs.coverage.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_SECURITY/${{ needs.security.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_DOCKER/${{ needs.docker-build.result }}/g" "$REPORT_FILE"
sed -i "s/JOB_QUALITY/${{ needs.quality.result }}/g" "$REPORT_FILE"

# Add test results section
cat >> "$REPORT_FILE" << 'EOF'

---

## 🧪 Test Results

### Summary Table

| Test Type | Total | Success | Failures | Errors | Skipped | Rate |
|-----------|-------|---------|----------|--------|---------|------|

EOF

# Add test results
get_test_summary "reports/unit-tests.xml" "Unit Tests" >> "$REPORT_FILE"
get_test_summary "reports/integration-tests.xml" "Integration Tests" >> "$REPORT_FILE"

# Add security section
cat >> "$REPORT_FILE" << 'EOF'

---

## 🔒 Security Scan

### Trivy Vulnerability Results

EOF

get_security_summary "reports/trivy-results.json" >> "$REPORT_FILE"

# Add coverage section
cat >> "$REPORT_FILE" << 'EOF'

---

## 📊 Code Coverage

### JaCoCo Coverage Results

EOF

get_coverage_summary "reports/jacoco.xml" >> "$REPORT_FILE"

# Add quality section
cat >> "$REPORT_FILE" << 'EOF'

---

## 🔧 Code Quality

### Static Analysis Results

EOF

get_quality_summary "reports/spotbugs.xml" >> "$REPORT_FILE"

# Add conclusion
cat >> "$REPORT_FILE" << 'EOF'

---

## ✅ Conclusion

Pipeline execution completed. 

**Next Steps:**
1. Review any failed jobs
2. Address security vulnerabilities if any
3. Ensure code coverage meets 80% target
4. Fix any code quality issues before merge

---

*Report automatically generated by CI Pipeline*
EOF

echo "Report generated: $REPORT_FILE"