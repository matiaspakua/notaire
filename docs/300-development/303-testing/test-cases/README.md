# Logical test cases (TC-*)

**ID scheme:** see Master Test Plan §3.3 (`TEST-PLAN.md`).

| ID | Role |
|----|------|
| `TC-*` | Optional **logical** test-case narrative (design / review) |
| `TS-nnnn` | **Automation** Playwright suite (authoritative list in `INVENTORY.md`) |
| Bruno / JUnit | **Automation** at API and unit/integration levels |

The `TC-001`…`TC-009` files in this folder are happy-path narratives for early
core use cases. They are **not** a complete catalog and are not CI-gated.
Prefer linking new work to CU + `TS-nnnn` / Bruno / JUnit in OpenSpec
`traceability.md`.

Login-focused design: [`../test-suite/LOGIN-TEST-SUITE.md`](../test-suite/LOGIN-TEST-SUITE.md).
Snapshot execution reports belong in CI artifacts, not permanent docs (#1041).
