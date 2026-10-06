#!/bin/bash

###############################################################################
# Notaire API-UI Alignment Audit Script
# Purpose: Extract endpoints, DTOs, forms, types and identify gaps
# Author: Claude Code Agent
# Date: 2026-06-16
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directories
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${PROJECT_ROOT}/backend-api"
FRONTEND_DIR="${PROJECT_ROOT}/frontend"
AUDIT_DIR="/tmp/notaire-audit-$(date +%Y%m%d_%H%M%S)"

# Create audit directory
mkdir -p "$AUDIT_DIR"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Notaire API-UI Alignment Audit${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Project Root: $PROJECT_ROOT"
echo "Audit Directory: $AUDIT_DIR"
echo "Start Time: $(date)"
echo ""

###############################################################################
# PHASE 1: Extract REST Endpoints from Backend
###############################################################################

echo -e "${YELLOW}[1/6] Extracting REST API Endpoints...${NC}"

# Find all @RequestMapping, @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
find "$BACKEND_DIR/src/main/java" -name "*Controller.java" -type f | while read -r file; do
    class_name=$(basename "$file" .java)
    request_path=$(grep -h "@RequestMapping\|@RestController" "$file" | grep -o '"/api/[^"]*"' | head -1 | tr -d '"')

    if [ -n "$request_path" ]; then
        # Extract all endpoint methods
        grep -E "@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@PatchMapping|@RequestMapping" "$file" | \
        while read -r mapping; do
            path=$(echo "$mapping" | grep -o '"/[^"]*"' | head -1 | tr -d '"')
            method=$(echo "$mapping" | grep -o "@[A-Za-z]*Mapping" | head -1 | sed 's/@//;s/Mapping//')
            if [ -n "$path" ]; then
                echo "${method:-GET} ${request_path}${path} (${class_name})"
            fi
        done
    fi
done | sort -u > "$AUDIT_DIR/backend_endpoints.txt"

endpoint_count=$(wc -l < "$AUDIT_DIR/backend_endpoints.txt")
echo -e "${GREEN}✓ Found ${endpoint_count} endpoints${NC}"

###############################################################################
# PHASE 2: Extract API Calls from Frontend
###############################################################################

echo -e "${YELLOW}[2/6] Extracting API Calls from Frontend...${NC}"

# Find all API calls: fetch, axios, apiClient
find "$FRONTEND_DIR/src" -name "*.ts" -o -name "*.tsx" | xargs grep -h "fetch\|axios\|apiClient\|api\." 2>/dev/null | \
grep -E "^\s*(fetch|axios|apiClient|api\.)" | \
sed 's/^[[:space:]]*//' | \
sort -u > "$AUDIT_DIR/frontend_api_calls_raw.txt"

# Extract URLs/paths from API calls
grep -o '/api/[^[:space:]]*' "$AUDIT_DIR/frontend_api_calls_raw.txt" 2>/dev/null | \
sort -u > "$AUDIT_DIR/frontend_api_paths.txt" || echo "No API paths found" > "$AUDIT_DIR/frontend_api_paths.txt"

api_call_count=$(wc -l < "$AUDIT_DIR/frontend_api_paths.txt")
echo -e "${GREEN}✓ Found ${api_call_count} API calls from frontend${NC}"

###############################################################################
# PHASE 3: Extract DTO Classes
###############################################################################

echo -e "${YELLOW}[3/6] Extracting DTO Classes...${NC}"

# Find all DTO classes (classes ending with DTO)
find "$BACKEND_DIR/src/main/java" -name "*DTO.java" -o -name "*Dto.java" | \
xargs grep -l "^public class" | \
while read -r file; do
    grep "^public class" "$file" | sed 's/.*class //;s/ .*//' | sed "s/$/ ($(basename $file))/"
done | sort -u > "$AUDIT_DIR/dto_classes.txt"

dto_count=$(wc -l < "$AUDIT_DIR/dto_classes.txt")
echo -e "${GREEN}✓ Found ${dto_count} DTO classes${NC}"

###############################################################################
# PHASE 4: Extract TypeScript Types
###############################################################################

echo -e "${YELLOW}[4/6] Extracting TypeScript Types...${NC}"

# Find all interfaces and types in TypeScript
find "$FRONTEND_DIR/src" \( -name "*.ts" -o -name "*.tsx" \) | \
xargs grep -h "^export interface\|^export type\|^interface\|^type" 2>/dev/null | \
sed 's/{.*//' | \
sort -u > "$AUDIT_DIR/ts_types.txt"

ts_type_count=$(wc -l < "$AUDIT_DIR/ts_types.txt")
echo -e "${GREEN}✓ Found ${ts_type_count} TypeScript types${NC}"

###############################################################################
# PHASE 5: Extract Form Components
###############################################################################

echo -e "${YELLOW}[5/6] Extracting Form Components...${NC}"

# Find all form-related files
find "$FRONTEND_DIR/src" -name "*form*.tsx" -o -name "*Form*.tsx" | \
sort -u > "$AUDIT_DIR/form_files.txt"

form_count=$(wc -l < "$AUDIT_DIR/form_files.txt")
echo -e "${GREEN}✓ Found ${form_count} form components${NC}"

###############################################################################
# PHASE 6: Identify Orphaned Endpoints
###############################################################################

echo -e "${YELLOW}[6/6] Identifying Orphaned Endpoints (gaps)...${NC}"

# Extract just the paths from backend endpoints
sed 's/.* //;s/ (.*//' "$AUDIT_DIR/backend_endpoints.txt" | sort -u > "$AUDIT_DIR/backend_paths_only.txt"

# Compare backend paths with frontend paths
comm -23 <(sort "$AUDIT_DIR/backend_paths_only.txt") <(sort "$AUDIT_DIR/frontend_api_paths.txt") > "$AUDIT_DIR/orphaned_endpoints.txt"

orphaned_count=$(wc -l < "$AUDIT_DIR/orphaned_endpoints.txt")
echo -e "${GREEN}✓ Identified ${orphaned_count} orphaned endpoints${NC}"

###############################################################################
# PHASE 7: Generate Report
###############################################################################

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Audit Report${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

cat > "$AUDIT_DIR/AUDIT_REPORT.txt" << EOF
================================================================================
                    NOTAIRE API-UI ALIGNMENT AUDIT REPORT
================================================================================

Generated: $(date)
Audit Directory: $AUDIT_DIR
Project Root: $PROJECT_ROOT

================================================================================
                              SUMMARY METRICS
================================================================================

Backend REST Endpoints:              ${endpoint_count}
Frontend API Calls:                  ${api_call_count}
DTO Classes:                         ${dto_count}
TypeScript Types:                    ${ts_type_count}
Form Components:                     ${form_count}

Orphaned Endpoints (Gaps):           ${orphaned_count}
Coverage Rate:                       $(( (endpoint_count - orphaned_count) * 100 / endpoint_count ))%

================================================================================
                            BACKEND ENDPOINTS
================================================================================

$(cat "$AUDIT_DIR/backend_endpoints.txt")

================================================================================
                         FRONTEND API CALLS
================================================================================

$(cat "$AUDIT_DIR/frontend_api_paths.txt")

================================================================================
                          ORPHANED ENDPOINTS (GAPS)
================================================================================

These endpoints exist in backend but are NOT called from frontend:

$(cat "$AUDIT_DIR/orphaned_endpoints.txt" || echo "None found")

================================================================================
                              DTO CLASSES
================================================================================

$(head -20 "$AUDIT_DIR/dto_classes.txt")
$([ $(wc -l < "$AUDIT_DIR/dto_classes.txt") -gt 20 ] && echo "... and $(($(wc -l < "$AUDIT_DIR/dto_classes.txt") - 20)) more")

================================================================================
                            TYPESCRIPT TYPES
================================================================================

$(head -20 "$AUDIT_DIR/ts_types.txt")
$([ $(wc -l < "$AUDIT_DIR/ts_types.txt") -gt 20 ] && echo "... and $(($(wc -l < "$AUDIT_DIR/ts_types.txt") - 20)) more")

================================================================================
                           FORM COMPONENTS
================================================================================

$(cat "$AUDIT_DIR/form_files.txt" | sed 's|.*/||')

================================================================================
                          NEXT STEPS
================================================================================

1. REVIEW ORPHANED ENDPOINTS
   See: $AUDIT_DIR/orphaned_endpoints.txt
   Decision: Implement UI / Keep internal / Remove

2. CREATE ENDPOINT REGISTRY
   Create: docs/200-architecture/203-design/ENDPOINT_REGISTRY.json
   Content: Map all endpoints with usage status

3. VALIDATE TYPE MAPPINGS
   Create: docs/200-architecture/203-design/TYPE_MAPPINGS.csv
   Content: DTO ↔ TypeScript type equivalence

4. DOCUMENT GAPS
   Action: Create GitHub issues for each gap
   Template: [API-UI-GAP] <endpoint-path>

5. COMMIT AND CREATE PR
   Command: git add . && git commit -m "docs(audit): ..."

================================================================================
                          FILES GENERATED
================================================================================

$AUDIT_DIR/
├── backend_endpoints.txt ................. All REST endpoints
├── frontend_api_calls_raw.txt ............ Raw API call lines
├── frontend_api_paths.txt ................ Extracted API paths
├── backend_paths_only.txt ................ Backend paths (no methods)
├── orphaned_endpoints.txt ................ GAPS - endpoints without UI
├── dto_classes.txt ....................... All DTO classes
├── ts_types.txt .......................... TypeScript types
├── form_files.txt ........................ Form components
└── AUDIT_REPORT.txt ...................... This report

================================================================================
                            ANALYSIS NOTES
================================================================================

• Endpoints found: ${endpoint_count}
• Frontend calls found: ${api_call_count}
• Coverage gap identified: ${orphaned_count} endpoints
• Action required: Review orphaned endpoints, create GitHub issues

See detailed files in: $AUDIT_DIR/

================================================================================
EOF

cat "$AUDIT_DIR/AUDIT_REPORT.txt"

###############################################################################
# Summary
###############################################################################

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Audit Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Results saved to: $AUDIT_DIR"
echo ""
echo "Key files:"
echo "  • Orphaned endpoints: $AUDIT_DIR/orphaned_endpoints.txt"
echo "  • All backends: $AUDIT_DIR/backend_endpoints.txt"
echo "  • Frontend calls: $AUDIT_DIR/frontend_api_paths.txt"
echo "  • Full report: $AUDIT_DIR/AUDIT_REPORT.txt"
echo ""
echo "Next step: Review orphaned endpoints and create GitHub issues"
echo ""
