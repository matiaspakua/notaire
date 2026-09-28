# Phase 3 — Specification for #{{ISSUE}} (Gate 1). Documents only, no code

Branch `{{BRANCH}}` and change `openspec/changes/{{CHANGE}}/` already exist.
Your triage is in `{{IO}}/triage.md` and `{{IO}}/triage.env` — read both first.
Never run mvn, npm or any build/test here: coverage and test results come from
the issue and triage, and a build started in this phase stalls it.

Fill the artifacts in this order. For EACH one, first run
`openspec instructions <artifact> --change {{CHANGE}}` and follow it, and look at
the same file in `openspec/changes/archive/2026-09-19-fix-bruno-login-field-names/`
as a model of the expected depth:

0. Copy config keys, paths and class names EXACTLY from `## Evidence` in triage.md
   (open the file if unsure). A wrong key name makes the spec wrong.
1. `proposal.md` — header table must show Issue `#{{ISSUE}}`, Use Case `{{USE_CASE}} — {{USE_CASE_TITLE}}`,
   branch `{{BRANCH}}`. If USE_CASE is NONE, state why no Use Case applies.
2. `specs/<capability>/spec.md` — one `### Requirement:` per behaviour, each with
   `#### Scenario:` blocks (WHEN/THEN) = the acceptance criteria from triage.
   Look in `openspec/specs/` for an existing capability first; if one fits, use
   `## MODIFIED Requirements` against it, otherwise `## ADDED Requirements`.
   Specify ONLY what the TODO criteria say. Never invent new behaviour (new
   startup checks, new modes, new error messages) — that is scope creep and is
   rejected in review. DONE criteria are not re-specified.
   One requirement per TODO criterion; each planned test from triage appears
   in a scenario and in traceability.md.
   ONLY for pure docs/ci changes with no behaviour (KIND={{KIND}}): skip specs and set
   `skip_specs: true` in `openspec/changes/{{CHANGE}}/.openspec.yaml`.
3. `design.md` — include Testing Strategy (which test classes, which level),
   Regression Strategy, Playwright Strategy (n/a + reason if UI_CHANGE=no),
   Deployment and Rollback.
4. `traceability.md` — Issue → Use Case → requirement → scenario → planned test →
   planned file. Leave commit/PR cells as `pending`.
5. `tasks.md` — keep all 12 mandatory groups; put the concrete work in group 4.
   Tick `[x]` only group 1 and 2 items that are already true.

Then check, and fix until both pass:

```text
openspec validate {{CHANGE}} --strict
bash scripts/validate-sdlc-plan.sh {{CHANGE}}
```

Do NOT commit — the foreman commits the change folder after the gate passes.
