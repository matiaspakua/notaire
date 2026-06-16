#!/bin/bash

###############################################################################
# Notaire API-UI Alignment Audit Script - IMPROVED
# Extracts endpoints and searches for their usage in frontend
###############################################################################

set -e

PROJECT_ROOT="/Users/matiasmiguez/workspace/notaire"
BACKEND_DIR="${PROJECT_ROOT}/backend-api"
FRONTEND_DIR="${PROJECT_ROOT}/frontend"
AUDIT_DIR="/tmp/notaire-audit-$(date +%Y%m%d_%H%M%S)"

mkdir -p "$AUDIT_DIR"

echo ""
echo "=========================================="
echo "  Notaire API-UI Alignment Audit (v2)"
echo "=========================================="
echo ""
echo "Audit Directory: $AUDIT_DIR"
echo ""

# ============================================================================
# PHASE 1: Extract REST Endpoints
# ============================================================================

echo "[1/5] Extracting REST API Endpoints..."

cat > "$AUDIT_DIR/extract_endpoints.py" << 'PYTHON'
import os
import re

api_dir = "/Users/matiasmiguez/workspace/notaire/backend-api/src/main/java/com/licensis/notaire/api"

endpoints = []

for filename in os.listdir(api_dir):
    if filename.endswith("Controller.java"):
        filepath = os.path.join(api_dir, filename)
        with open(filepath, 'r') as f:
            content = f.read()

            # Extract class-level RequestMapping
            class_path_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
            class_path = class_path_match.group(1) if class_path_match else ""

            # Extract RestController mapping
            if not class_path:
                rest_match = re.search(r'@RestController\s*\n\s*@RequestMapping\("([^"]+)"\)', content)
                if rest_match:
                    class_path = rest_match.group(1)

            # Extract all method-level mappings
            pattern = r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\("([^"]+)"\)'
            for match in re.finditer(pattern, content):
                method = match.group(1).replace("Mapping", "")
                path = match.group(2)
                full_path = class_path + path if class_path else path
                endpoints.append(f"{method:6s} {full_path}")

# Write endpoints
with open("/tmp/notaire-audit-endpoints.txt", "w") as f:
    for ep in sorted(set(endpoints)):
        f.write(ep + "\n")

print(f"Found {len(set(endpoints))} endpoints")
PYTHON

python3 "$AUDIT_DIR/extract_endpoints.py"

# Copy to audit directory
cp /tmp/notaire-audit-endpoints.txt "$AUDIT_DIR/backend_endpoints.txt"
endpoint_count=$(wc -l < "$AUDIT_DIR/backend_endpoints.txt")
echo "✓ Found $endpoint_count endpoints"

# ============================================================================
# PHASE 2: Search for API usage in Frontend
# ============================================================================

echo "[2/5] Searching for API calls in Frontend..."

# Search for fetch, axios, apiClient patterns
find "$FRONTEND_DIR/src" \( -name "*.ts" -o -name "*.tsx" \) -type f 2>/dev/null | \
while read file; do
    grep -o '/api/[a-zA-Z0-9\-/{}]*' "$file" 2>/dev/null || true
done | sort -u > "$AUDIT_DIR/frontend_api_paths.txt"

api_count=$(wc -l < "$AUDIT_DIR/frontend_api_paths.txt")
echo "✓ Found $api_count API path references in frontend"

# ============================================================================
# PHASE 3: Find Orphaned Endpoints
# ============================================================================

echo "[3/5] Identifying orphaned endpoints..."

# Extract just paths from backend endpoints
awk '{print $2}' "$AUDIT_DIR/backend_endpoints.txt" | sort -u > "$AUDIT_DIR/backend_paths.txt"

# Compare with frontend
comm -23 <(sort "$AUDIT_DIR/backend_paths.txt") <(sort "$AUDIT_DIR/frontend_api_paths.txt") > "$AUDIT_DIR/orphaned_endpoints.txt"

orphaned_count=$(wc -l < "$AUDIT_DIR/orphaned_endpoints.txt")
echo "✓ Identified $orphaned_count orphaned endpoints"

# ============================================================================
# PHASE 4: Extract DTO Classes
# ============================================================================

echo "[4/5] Extracting DTO classes..."

find "$BACKEND_DIR/src/main/java" -name "*DTO.java" -o -name "*Dto.java" 2>/dev/null | \
while read file; do
    grep "^public class" "$file" | sed 's/.*class //;s/ .*//'
done | sort -u > "$AUDIT_DIR/dto_classes.txt"

dto_count=$(wc -l < "$AUDIT_DIR/dto_classes.txt")
echo "✓ Found $dto_count DTO classes"

# ============================================================================
# PHASE 5: Extract TypeScript Types
# ============================================================================

echo "[5/5] Extracting TypeScript types..."

find "$FRONTEND_DIR/src" \( -name "*.ts" -o -name "*.tsx" \) -type f 2>/dev/null | \
xargs grep -h "^export interface\|^export type" 2>/dev/null | \
sed 's/{.*//' | sort -u > "$AUDIT_DIR/ts_types.txt"

ts_count=$(wc -l < "$AUDIT_DIR/ts_types.txt")
echo "✓ Found $ts_count TypeScript types"

# ============================================================================
# PHASE 6: Generate Report
# ============================================================================

echo ""
echo "=========================================="
echo "  Summary"
echo "=========================================="
echo ""

echo "Backend endpoints found:  $endpoint_count"
echo "Frontend API calls found: $api_count"
echo "DTO classes found:        $dto_count"
echo "TypeScript types found:   $ts_count"
echo ""
echo "Coverage gaps identified: $orphaned_count"
[ $endpoint_count -gt 0 ] && echo "Coverage rate: $((100 * (endpoint_count - orphaned_count) / endpoint_count))%"
echo ""

# ============================================================================
# Create detailed report
# ============================================================================

cat > "$AUDIT_DIR/AUDIT_REPORT.txt" << EOF
================================================================================
                    API-UI ALIGNMENT AUDIT REPORT
================================================================================

Date: $(date)
Project: Notaire
Phase: 7 - Coverage Gap Analysis

================================================================================
                              SUMMARY
================================================================================

Backend REST Endpoints:       $endpoint_count
Frontend API References:      $api_count
Orphaned Endpoints (GAPS):    $orphaned_count
Coverage Rate:                $((100 * (endpoint_count - orphaned_count) / endpoint_count))%

DTO Classes:                  $dto_count
TypeScript Types:             $ts_count

================================================================================
                        ORPHANED ENDPOINTS (GAPS)
================================================================================

These endpoints exist in backend but are NOT called from frontend:

$(cat "$AUDIT_DIR/orphaned_endpoints.txt")

================================================================================
                            ALL ENDPOINTS
================================================================================

$(cat "$AUDIT_DIR/backend_endpoints.txt")

================================================================================
                      FRONTEND API REFERENCES
================================================================================

$(cat "$AUDIT_DIR/frontend_api_paths.txt")

================================================================================
                           NEXT STEPS
================================================================================

1. REVIEW GAPS
   File: $AUDIT_DIR/orphaned_endpoints.txt

2. DECIDE FOR EACH GAP:
   • Implement in UI
   • Keep internal
   • Remove from API

3. CREATE ENDPOINT REGISTRY
   Create: docs/03-api/ENDPOINT_REGISTRY.json

4. CREATE ISSUES
   Template: [API-UI-GAP] <endpoint_path>

5. COMMIT CHANGES

================================================================================
EOF

cat "$AUDIT_DIR/AUDIT_REPORT.txt"

echo ""
echo "Files generated in: $AUDIT_DIR"
echo ""
echo "Key files:"
echo "  - Orphaned endpoints: $AUDIT_DIR/orphaned_endpoints.txt"
echo "  - All endpoints:      $AUDIT_DIR/backend_endpoints.txt"
echo "  - Frontend calls:     $AUDIT_DIR/frontend_api_paths.txt"
echo "  - Full report:        $AUDIT_DIR/AUDIT_REPORT.txt"
echo ""
