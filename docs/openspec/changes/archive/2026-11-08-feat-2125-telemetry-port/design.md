> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2121 factory kit. Prior #2122 harness, #2123 Spec Kit, #2124 AI-DLC boundary.

## Goals / Non-Goals

**Goals:** Safe optional telemetry for factory lead-time/cost without dual orchestration or secret leakage.
**Non-Goals:** Hosted collector; product OTel; GenAI conventions; full OpenFeature SDK.

## Decisions

stdlib-only runtime; importlib optional OTel; exporter=none only; MemoryTelemetry for hook unit tests; OpenFeature as stub not SDK.

## Riesgos / Trade-offs

Optional venv drift — pin versions in optional/telemetry.txt and install from verify.sh.

## Testing Strategy

test_telemetry_port.py and test_feature_flags.py; bash framework/ai-sdlc/verify.sh

## Regression Strategy

kit independence stdlib scan; existing evidence/cost tests

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
