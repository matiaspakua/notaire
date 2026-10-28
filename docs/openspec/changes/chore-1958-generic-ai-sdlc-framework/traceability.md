# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
| ------ | ----------- | -------- |
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1958-generic-ai-sdlc-framework/` | passed |
| Branch | `chore/1958_generic_ai_sdlc_framework` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
| --------------------------------- | ------ | -------- |
| a required gate fails | framework/ai-sdlc/tests | pending |
| a red gate expects failure | framework/ai-sdlc/tests | pending |
| a path-scoped gate with no matching change | framework/ai-sdlc/tests | pending |
| ledger after a stage run | framework/ai-sdlc/tests | pending |
| report over the ledger | framework/ai-sdlc/tests | pending |
| a hand-added baseline line | framework/ai-sdlc/tests | pending |
| a shrunk baseline | framework/ai-sdlc/tests | pending |
| a JSON suppression count grows | framework/ai-sdlc/tests | pending |
| a coverage floor is missed | framework/ai-sdlc/tests | pending |
| duplicate skill names | framework/ai-sdlc/tests | pending |
| a project path in a generic skill | framework/ai-sdlc/tests | pending |
| over budget | framework/ai-sdlc/tests | pending |
| within budget | framework/ai-sdlc/tests | pending |
| non-conventional commit | framework/ai-sdlc/tests | pending |
| production change without tests | framework/ai-sdlc/tests | pending |
| unfilled section | framework/ai-sdlc/tests | pending |
| invented issue number | framework/ai-sdlc/tests | pending |
| stack-independent run | framework/ai-sdlc/tests | pending |
| no adopter path in the kit | framework/ai-sdlc/tests | pending |
| Notaire profile is valid | framework/ai-sdlc/tests | pending |
| Process Checks run the ratchet | framework/ai-sdlc/tests | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
| ---------- | --------- | -------- |
| `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md` | no | pending |
| `framework/ai-sdlc/README.md` | no | pending |
| `CONSTITUTION.md` | no | pending |
| `.claude/skills/README.md` | no | pending |
| `github-page/README.md` | no | pending |
| `docs/300-development/README.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
| ------ | ----------- | -------- | ---------- |
| 1 | Issue + Specification + Acceptance Criteria | yes | #1958, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
