# Validation — fix-1047 OpenSpec draft

| Check | Result |
|-------|--------|
| Pattern reference | `internal/openspec-1048/` + `openspec/schemas/notaire-sdlc` |
| Schema | `notaire-sdlc` |
| Live Issue | #1047 OPEN (`gh issue view 1047`) |
| Use Cases | CU74 – Performance and Caching Strategy; CU76 – Quality Assurance and Testing Infrastructure |
| CI evidence | `performance-test.yml` → missing `performance-test/k6/load-test.js` (deleted `b822a18`); Actions fail at Run k6; asset unittest `FileNotFoundError` |
| API note | Login DTO is English `{ name, password }` — rewrite required, not blind restore |
| SLO note | CU74 objective p95 &lt; 2s; threshold MUST be p95≤2000ms + error rate &lt;1% |
| Scenarios | 7 × `#### Scenario:` |
| Mandatory task groups | 12 present |
| Serialize | #1048 on main (`2967ec32`); implement #1047 |
| Repo branch / PR / push | `cursor/fix-1047-k6-load-test-69d3` (implement) |
| `bash scripts/validate-sdlc-plan.sh fix-1047-k6-load-test` | **PASS** (temp copy into `openspec/changes/`, then removed; no repo leave-behind) |

As-of: 2026-10-03.
