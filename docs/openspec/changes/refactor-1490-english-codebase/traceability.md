# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1490 | open |
| Use Case | CU75 — Database Management and Migrations | exists |
| Specification | `docs/openspec/changes/refactor-1490-english-codebase/` | written |
| Branch | `cursor/english-codebase-01e0` | created from `origin/main` |
| Tasks | `tasks.md` | in progress |
| Commits | `9c536900`, `329247ba`, `86b51c34`, `93300933`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `2c345ab5`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `e42e1487`, `abde581c`, `68d84c39`, `c58be179`, `dd015ac3`, `797b268c`, `ffb8fdd5`, `88a7e431`, `ffe5aaa4`, `db6a4876`, `7a9458cb`, `c7d87636`, `e48ddf2a`, `ec567185`, `d1aa0d4c`, `df1c22fc`, `14abf883`, `315c83ab`, `0cb342a1`, `503354fc`, `be66ad75`, `51c25a65`, `298d04df`, `676003db`, `4177e611`, `bd883977`, `aba4adb4`, `ee51a917`, `8908af20`, `ee860266`, `01771ab1`, `fdcf0788`, `66c8a54d`, `c690d487`, `7a10f083`, `471334a5`, `8b1b4322`, `e030d2a1`, `9935ac81`, `4470dbfc`, `eb3ccb6f`, `49c85360`, `99b66958`, `602d7019`, `fd6fa760`, `6ee0f0c1`, `0e320a6f`, `f6a28763`, `88602fcc`, `0a259320`, `0523aae6`, `fe5ee462`, `6dd60865`, `b9840ded`, `0a82b3b5`, `74671e88`, `8352fccd`, `509c9a6c`, `b418e6ce`, `2a860c47`, `ce5010fc`, `f3d76e63`, `8067d797`, `1f9bf2f9`, `03bfc567`, `1ba4d9fa`, `87b763a0`, `85a6d8cd`, `ceb44c77`, `48c6a8c1`, `9cef2ec7`, `e525076f`, `04e51c02`, `27b151eb`, `f5cba10d`, `4cf2154f`, `c284f284`, `ce6b5232`, `78ea0fd3`, `ee6039ca`, `525c7d5a`, `d19f616e`, `da546cc9`, `d4a2835f`, `d9c52f61`, `12aecc5a`, `c8b59dae`, `7f310e87`, `75ac45cb` | created |
| Pull Request | #1495 | draft |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Users login path is English | `EnglishSurfaceRatchetTest` | passing |
| Spanish resource segment is not mapped | `EnglishSurfaceRatchetTest` | passing |
| People screen lives at the English route | dashboard folder `/dashboard/people` plus nav unit test | passing |
| Navigation hrefs do not use Spanish segments | `EnglishSurfaceRatchetTest` | passing |
| Identifications table is English | `EnglishSurfaceRatchetTest` | passing |
| Applied migrations are immutable | `git diff` against `origin/main` for V1–V44 | passing |
| Spanish catalog is still the Spanish locale | `frontend/src/tests/unit/i18n.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | `329247ba`, `99b66958` |
| `CHANGELOG.md` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `797b268c`, `88a7e431`, `db6a4876`, `c7d87636`, `ec567185`, `df1c22fc`, `0cb342a1`, `be66ad75`, `298d04df`, `4177e611`, `aba4adb4`, `8908af20`, `01771ab1`, `66c8a54d`, `7a10f083`, `8b1b4322`, `9935ac81`, `eb3ccb6f`, `99b66958`, `fd6fa760`, `0e320a6f`, `88602fcc`, `0523aae6`, `6dd60865`, `0a82b3b5`, `8352fccd`, `b418e6ce`, `ce5010fc`, `8067d797`, `03bfc567`, `87b763a0`, `ceb44c77`, `9cef2ec7`, `04e51c02`, `f5cba10d`, `c284f284`, `78ea0fd3`, `525c7d5a`, `da546cc9`, `d9c52f61`, `c8b59dae`, `75ac45cb` |
| `backend-api/openapi/openapi.yaml` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `ec567185`, `0cb342a1`, `be66ad75`, `aba4adb4`, `01771ab1`, `66c8a54d`, `7a10f083`, `eb3ccb6f`, `b418e6ce`, `ce5010fc` |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | `4a3afc10`, `9935ac81` |
| `backend-api/api-test/COVERAGE.md` | yes | `329247ba`, `0523aae6` |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | `8067d797` |
| `docs/300-development/303-testing/api-test/README.md` | yes | `8067d797` |
| `docs/200-architecture/201-SAD/sad.md` | yes | `03bfc567` |
| `docs/200-architecture/202-ADR/ADR-013-audit-trail.md` | yes | `03bfc567` |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-014-workflow-engine.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-015-internationalization.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-001-microservices-architecture.md` | yes | `ceb44c77` |
| `docs/200-architecture/203-design/README.md` | yes | `ceb44c77` |
| `docs/200-architecture/202-ADR/ADR-006-testing-strategy.md` | yes | `9cef2ec7` |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | yes | `9cef2ec7` |
| `docs/300-development/README.md` | yes | `04e51c02` |
| `docs/300-development/303-testing/README.md` | yes | `04e51c02` |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md` | yes | `f5cba10d` |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md` | yes | `c284f284` |
| `docs/200-architecture/202-ADR/ADR-003-rest-api-versioning.md` | yes | `78ea0fd3` |
| `docs/200-architecture/202-ADR/ADR-004-database-migration.md` | yes | `da546cc9` |
| `docs/200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md` | yes | `d9c52f61` |
| `docs/200-architecture/202-ADR/README.md` | yes | `c8b59dae` |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes | `75ac45cb` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1490, this change folder |
| 2 | Failing tests written, test cases designed | yes | `EnglishSurfaceRatchetTest` red run, then green |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Cloud agent branch policy requires the prefix `cursor/` and the suffix `-01e0`, so the branch is `cursor/english-codebase-01e0` rather than `refactor/1490_english_codebase`. No other Constitution step is skipped. Issue labeling `in-progress` failed with `Resource not accessible by integration`; the issue remains open as #1490.
