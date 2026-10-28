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
|------|-----------|--------|
| Issue | #1964 | in-progress |
| Use Case | CU88 – Asistente IA de la gestión | created |
| Specification | `docs/openspec/changes/feat-1964-ai-assistant/` | proposed (Gate 1 owner-pending) |
| Branch | `feat/1964_ai_assistant` | created |
| Tasks | `tasks.md` | phase 0 planning done; implementation pending Gate 1 |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Assistant disabled by default | `AssistantStatusIntegrationTest`, `assistant.spec.ts` | pending |
| Model server down | `AssistantUnavailableIntegrationTest`, `assistant.spec.ts` | pending |
| Remote endpoint refused | `AssistantEndpointGuardTest` | pending |
| Ask about the management on screen | `AssistantChatSseIntegrationTest`, `assistant.spec.ts` | pending |
| User stops the answer | `assistantStream.test.ts`, `AssistantChatSseIntegrationTest` | pending |
| Data the REST API denies is denied to the assistant | `NotaireToolAdvisorTest`, `AssistantToolAuthorizationIntegrationTest` | pending |
| Prompt injection inside a document | `AssistantPromptTemplateTest`, `testing/assistant-evals/injection/` | pending |
| Proposed action needs confirmation | `NotaireToolAdvisorTest`, `assistant-proposal.spec.ts` | pending (phase 3) |
| Interaction is audited without content | `AssistantAuditIntegrationTest`, `AssistantObservationContentTest` | pending |
| Conversation belongs to its owner | `AssistantConversationOwnershipIntegrationTest` | pending |
| One trace per question | `AssistantTracePropagationIntegrationTest` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU88 – Asistente IA de la gestión.md` | yes (created) | pending |
| `docs/200-architecture/202-ADR/ADR-031-local-ai-assistant.md` | yes (Proposed) | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | owner-pending | #1964, this change, CU88, ADR-031 |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None taken. Note: this intent PR leaves the implementation task groups 3–8 unchecked by design
(Gate 1 is owner-pending); `check-openspec-archive-hygiene.py` reports them until phase 1 lands on
this change, unless the Owner applies `sdlc-exception` to the intent PR.
