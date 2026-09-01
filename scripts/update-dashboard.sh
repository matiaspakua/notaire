#!/bin/bash

# Notaire Migration Dashboard Updater
# This script helps categorize issues for the migration dashboard

set -e

echo "🔍 Analyzing Notaire migration status..."
echo "========================================"

# Get all open issues
echo "Fetching open issues..."
OPEN_ISSUES=$(gh issue list --repo matiaspakua/notaire --limit 100 --state open --json number,title,labels,state)

TOTAL_ISSUES=$(echo $OPEN_ISSUES | jq length)
echo "Found $TOTAL_ISSUES open issues"

# Categorize issues by phase
echo ""
echo "📊 Categorizing issues by migration phase..."
echo "============================================"

# Phase 4: Modern Frontend
echo ""
echo "Phase 4: Modern Frontend Issues"
echo "-------------------------------"
PHASE4_ISSUES=0
for issue in $(echo $OPEN_ISSUES | jq -r '.[] | @base64'); do
    _jq() {
        echo ${issue} | base64 --decode | jq -r ${1}
    }
    
    number=$(_jq '.number')
    title=$(_jq '.title')
    labels=$(_jq '.labels | map(.name) | join(",")')
    
    # Check for frontend-related labels or keywords
    if [[ "$labels" =~ "FRONTEND" ]] || [[ "$title" =~ "frontend" ]] || [[ "$title" =~ "UI" ]] || [[ "$title" =~ "Next.js" ]] || [[ "$title" =~ "React" ]]; then
        echo "#$number: $title"
        PHASE4_ISSUES=$((PHASE4_ISSUES + 1))
    fi
done
echo "Total Phase 4 issues: $PHASE4_ISSUES"

# Phase 6: Technical Debt
echo ""
echo "Phase 6: Technical Debt & Refactoring"
echo "-------------------------------------"
PHASE6_ISSUES=0
for issue in $(echo $OPEN_ISSUES | jq -r '.[] | @base64'); do
    _jq() {
        echo ${issue} | base64 --decode | jq -r ${1}
    }
    
    number=$(_jq '.number')
    title=$(_jq '.title')
    labels=$(_jq '.labels | map(.name) | join(",")')
    
    # Check for refactoring, legacy, or technical debt
    if [[ "$labels" =~ "refactor" ]] || [[ "$title" =~ "legacy" ]] || [[ "$title" =~ "technical debt" ]] || [[ "$title" =~ "refactor" ]] || [[ "$title" =~ "jpa" ]] || [[ "$labels" =~ "BACKEND" ]] && [[ ! "$labels" =~ "FRONTEND" ]]; then
        echo "#$number: $title"
        PHASE6_ISSUES=$((PHASE6_ISSUES + 1))
    fi
done
echo "Total Phase 6 issues: $PHASE6_ISSUES"

# DevOps & Infrastructure
echo ""
echo "DevOps & Infrastructure Issues"
echo "------------------------------"
DEVOPS_ISSUES=0
for issue in $(echo $OPEN_ISSUES | jq -r '.[] | @base64'); do
    _jq() {
        echo ${issue} | base64 --decode | jq -r ${1}
    }
    
    number=$(_jq '.number')
    title=$(_jq '.title')
    labels=$(_jq '.labels | map(.name) | join(",")')
    
    if [[ "$labels" =~ "DEVOPS" ]] || [[ "$title" =~ "CI" ]] || [[ "$title" =~ "CD" ]] || [[ "$title" =~ "Docker" ]] || [[ "$title" =~ "infra" ]]; then
        echo "#$number: $title"
        DEVOPS_ISSUES=$((DEVOPS_ISSUES + 1))
    fi
done
echo "Total DevOps issues: $DEVOPS_ISSUES"

# Testing & Quality
echo ""
echo "Testing & Quality Issues"
echo "------------------------"
TEST_ISSUES=0
for issue in $(echo $OPEN_ISSUES | jq -r '.[] | @base64'); do
    _jq() {
        echo ${issue} | base64 --decode | jq -r ${1}
    }
    
    number=$(_jq '.number')
    title=$(_jq '.title')
    labels=$(_jq '.labels | map(.name) | join(",")')
    
    if [[ "$labels" =~ "TEST" ]] || [[ "$title" =~ "test" ]] || [[ "$title" =~ "Test" ]] || [[ "$title" =~ "coverage" ]]; then
        echo "#$number: $title"
        TEST_ISSUES=$((TEST_ISSUES + 1))
    fi
done
echo "Total Testing issues: $TEST_ISSUES"

echo ""
echo "📈 Summary"
echo "=========="
echo "Phase 4 (Frontend): $PHASE4_ISSUES issues"
echo "Phase 6 (Technical Debt): $PHASE6_ISSUES issues"
echo "DevOps & Infrastructure: $DEVOPS_ISSUES issues"
echo "Testing & Quality: $TEST_ISSUES issues"
echo "Total Categorized: $((PHASE4_ISSUES + PHASE6_ISSUES + DEVOPS_ISSUES + TEST_ISSUES)) issues"
echo "Uncategorized: $((TOTAL_ISSUES - (PHASE4_ISSUES + PHASE6_ISSUES + DEVOPS_ISSUES + TEST_ISSUES))) issues"

echo ""
echo "✅ Dashboard analysis complete!"
echo "Update MIGRATION-DASHBOARD.md and MIGRATION-BACKLOG.md with these findings."