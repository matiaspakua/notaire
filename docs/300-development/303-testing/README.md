# Testing — Notaire

Process knowledge for verification and validation. The **Master Test Plan** is
[`TEST-PLAN.md`](TEST-PLAN.md). Suite volumes live in generated
[`INVENTORY.md`](INVENTORY.md) — regenerate after adding or renaming suites:

```bash
python3 workspace/sdlc/generate-testing-inventory.py
python3 workspace/sdlc/validate-testing-inventory.py
```

Black-box runners and how-to guides: [`testing/README.md`](../../../testing/README.md).

```text
Unit → Integration → API (Bruno) → Frontend (Vitest) → E2E UI (Playwright, TS-nnnn)
```

## Documents

| Document | Contents |
|----------|----------|
| [`TEST-PLAN.md`](TEST-PLAN.md) | **Master Test Plan** — levels, IDs, floors, exit criteria |
| [`PROCESS-ARCHITECTURE.md`](PROCESS-ARCHITECTURE.md) | Process boxes, owners, CI triggers |
| [`INVENTORY.md`](INVENTORY.md) | Living suite inventory (generated) |
| [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) | CU → controller → Bruno drift guard |
| [`E2E-TEST-MAPPING.md`](E2E-TEST-MAPPING.md) | CU ↔ TS narrative mapping |
| [`TESTING-PATTERNS.md`](TESTING-PATTERNS.md) | Fixture patterns by level |
| [`FRONTEND-TESTING-GUIDE.md`](FRONTEND-TESTING-GUIDE.md) | Vitest + Playwright conventions |
| [`test-coverage/TEST-COVERAGE-STRATEGY.md`](test-coverage/TEST-COVERAGE-STRATEGY.md) | Coverage strategy |
| [`ADR-006`](../../200-architecture/202-ADR/ADR-006-testing-strategy.md) | Architecture decision |

## Quick start

```bash
mvn test -pl backend-api
cd frontend && npm test
bash testing/scripts/run.sh database
bash workspace/stack/start.sh   # then:
bash testing/scripts/run.sh e2e
bash workspace/sdlc/preflight.sh --full
```

## Coverage floors

Enforced floors only (see MTP §4):

| Tool | Floor |
|------|-------|
| JaCoCo | 80% line / 65% branch |
| Vitest | raise-only 14/9/10/14 (statements/branches/functions/lines) |
| Aspirational | 80% / 80% backend branch; 80% Vitest (#1246) |

## CI/CD

[`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md). Swing E2E retired (ADR-012).

## Navigation

- [← Development](../)
- [Master Test Plan](TEST-PLAN.md)
- [Architecture](../../200-architecture/)
