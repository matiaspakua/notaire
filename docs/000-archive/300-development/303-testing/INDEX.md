# Integration Test Suite - Documentation Index

Complete guide to Notaire's centralized testing framework.

## Quick Navigation

### 🚀 Getting Started
1. **[README.md](README.md)** — Quick start guide, usage examples, troubleshooting
2. **[EXAMPLE_REPORT.md](EXAMPLE_REPORT.md)** — Sample test execution report

### 📋 Detailed Documentation
3. **[TEST_STRATEGY.md](../../../300-development/303-testing/TEST_STRATEGY.md)** (current version; superseded this archived copy) — Comprehensive testing strategy
4. **[api-test/API_TESTING_GUIDE.md](../../../300-development/303-testing/api-test/API_TESTING_GUIDE.md)** (current version; superseded this archived copy) — HTTP API testing details

### 🛠️ Scripts & Tools
5. **[run-all-tests.sh](../../../../testing/run-all-tests.sh)** — Master test runner (22.7 KB)
6. **[generate-coverage-report.sh](../../../../testing/generate-coverage-report.sh)** — Coverage analyzer

---

## File Descriptions

### Core Documentation

**README.md** (3 KB)
- Quick start (3 steps to run all tests)
- Test types overview
- Common commands
- Troubleshooting guide
- Links to detailed docs

**TEST_STRATEGY.md** (15 KB)
- Complete testing strategy
- Test pyramid visualization
- Per-layer detailed guide:
  - Unit tests (8+ classes)
  - Integration tests (~40 cases)
  - Client tests (2 classes)
  - HTTP tests (8 scripts)
  - E2E tests (7 suites, 30-40 cases)
- Architecture diagrams
- CI/CD integration info
- Best practices

**EXAMPLE_REPORT.md** (12 KB)
- Sample markdown report output
- Test execution results
- Code coverage breakdown
- Quality gates status
- Performance metrics
- Recommendations

### API Testing

**http/API_TESTING_GUIDE.md**
- REST endpoint documentation
- Authentication flow
- Example requests/responses
- Error codes and handling
- Validation rules

### Executable Scripts

**run-all-tests.sh** (22.7 KB)
- Master test orchestrator
- Executes all test suites:
  - Unit tests
  - Integration tests
  - Client tests
  - HTTP tests
  - E2E tests
- Generates consolidated markdown report
- Flags: `--unit-only`, `--skip-robot`, `--skip-http`, `--coverage`

**generate-coverage-report.sh** (6.2 KB)
- JaCoCo report generator
- Per-package coverage analysis
- Identifies uncovered areas
- HTML report generation
- Threshold validation (80%)

---

## Test Counts by Category

| Category | Files | Tests | Est. Duration |
|----------|-------|-------|---------------|
| **Unit** | 8 | 45+ | 4s |
| **Integration** | 5 | 44+ | 28s |
| **Client** | 2 | 10+ | 2s |
| **HTTP** | 8 | 32+ | 14s |
| **E2E** | 7 | 33+ | 5-10m |
| **TOTAL** | 30 | 160+ | 10-15m |

---

## Directory Structure

```
integration-test/
├── README.md                    ← Start here
├── TEST_STRATEGY.md            ← Detailed testing guide
├── EXAMPLE_REPORT.md           ← Sample output
├── INDEX.md                    ← This file
│
├── run-all-tests.sh            ← Master test runner
├── generate-coverage-report.sh ← Coverage analyzer
│
├── http/
│   ├── API_TESTING_GUIDE.md
│   ├── test-all-endpoints-v2.sh
│   ├── 01-auth.sh
│   ├── 02-usuarios.sh
│   ├── 03-conceptos.sh
│   ├── 04-personas.sh
│   ├── 05-tramites.sh
│   ├── 06-escrituras.sh
│   ├── 07-presupuestos.sh
│   └── 08-items.sh
│
├── e2e-swing/
│   ├── .venv/                  ← Python virtual environment
│   ├── tests/
│   │   ├── login_e2e.robot
│   │   ├── principal_navigation_e2e.robot
│   │   ├── administracion_e2e.robot
│   │   ├── clientes_e2e.robot
│   │   ├── gestiones_e2e.robot
│   │   ├── presupuestos_e2e.robot
│   │   └── protocolo_e2e.robot
│   ├── resources/
│   │   └── common.resource     ← Shared Robot keywords
│   └── requirements.txt
│
├── integration/
│   ├── e2e-login-and-stack.sh
│   └── README.md
│
└── reports/                    ← Generated after running tests
    ├── test-report-*.md       ← Consolidated markdown report
    ├── coverage-report.md     ← Coverage analysis
    ├── coverage.txt
    └── logs/
        ├── unit-tests.log
        ├── integration-tests.log
        ├── client-tests.log
        ├── http-tests.log
        ├── e2e-tests.log
        └── robot-output/      ← E2E HTML reports
            ├── report.html
            ├── log.html
            └── output.xml
```

---

## How to Use This Index

### For New Team Members
1. Read `README.md` (5 min)
2. Run `bash run-all-tests.sh --unit-only` (10s)
3. Review `EXAMPLE_REPORT.md` (10 min)
4. Read `TEST_STRATEGY.md` § on test type of interest

### For Quick Test Runs
- Unit only: `bash run-all-tests.sh --unit-only`
- No E2E: `bash run-all-tests.sh --skip-robot`
- Coverage: `bash generate-coverage-report.sh`

### For Full Understanding
1. `README.md` — Quick reference
2. `TEST_STRATEGY.md` — Deep dive
3. `EXAMPLE_REPORT.md` — Expected output
4. Script contents — Implementation details

---

## Commands Reference

```bash
# All tests (10-15 minutes)
cd integration-test && bash run-all-tests.sh

# Unit tests only (5 seconds)
bash run-all-tests.sh --unit-only

# Skip E2E (2 minutes)
bash run-all-tests.sh --skip-robot

# Coverage report
bash generate-coverage-report.sh

# View reports
cat reports/test-report-*.md
open backend-api/target/site/jacoco/index.html
```

---

## Key Statistics

- **Test Files Created:** 4 (README, TEST_STRATEGY, EXAMPLE_REPORT, this INDEX)
- **Scripts Created:** 2 (run-all-tests.sh, generate-coverage-report.sh)
- **Test Coverage:** 100+ tests across 5 categories
- **Expected Duration:** 10-15 minutes (full suite)
- **Code Coverage Requirement:** 80% minimum (enforced by JaCoCo)
- **CI/CD Integration:** GitHub Actions configured

---

## Next Steps

1. **Run the test suite:** `bash run-all-tests.sh`
2. **Review the report:** Check `reports/test-report-*.md`
3. **Check coverage:** Open `backend-api/target/site/jacoco/index.html`
4. **Configure CI:** GitHub Actions workflows ready
5. **Monitor trends:** Track coverage and test results over time

---

## Documentation Versions

| File | Version | Updated | Status |
|------|---------|---------|--------|
| README.md | 1.0 | 2026-04-14 | ✓ Current |
| TEST_STRATEGY.md | 1.0 | 2026-04-14 | ✓ Current |
| EXAMPLE_REPORT.md | 1.0 | 2026-04-14 | ✓ Current |
| run-all-tests.sh | 1.0 | 2026-04-14 | ✓ Current |
| generate-coverage-report.sh | 1.0 | 2026-04-14 | ✓ Current |

---

**Last Updated:** 2026-04-14  
**Maintained By:** Development Team  
**Version:** 1.0
