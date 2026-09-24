# Docs: DRY/consistency audit of SDLC governance layer

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1033 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `docs/1033_sdlc_governance_dry_audit` |
| Gate 1 status | passed |

## Objetivo

The project's own SDLC governance layer (`CONSTITUTION.md`, `AGENTS.md`,
`CLAUDE.md`, `.claude/rules/*`, `.claude/skills/*`) is supposed to be a single,
DRY, internally consistent source of truth per CONSTITUTION.md §12. An audit
found it has drifted in three concrete, mechanically verifiable ways: the
70%-floor/80%-target coverage distinction that `code-quality.md` carefully
documents is restated as a flat "80%" gate in two other rule files, one skill
still references a module that was removed from the repo, and a skill actually
used in a merged change is missing from the canonical skill catalog. None of
these affect application behavior, but they undermine the "one source of
truth" claim the Constitution makes about itself.

## What Changes

- `AGENTS.md`: correct the coverage comment on `mvn jacoco:check` to state the
  enforced ratchet floor (70% line / 25% branch) instead of a bare "≥ 80%".
- `.claude/rules/ai-agent-workflow.md`: reword the two "Coverage ≥ 80%
  (JaCoCo)" lines (Step 5 quality gates and the PR description template) to
  match the floor-vs-target distinction already correct in `code-quality.md`
  and `CLAUDE.md`.
- `.claude/skills/maven-build/SKILL.md`: remove the `frontend-swing` command
  example and catalog entry — that module was deleted from the repository
  (`CLAUDE.md` §Modules: "Removed ... do not recreate it").
- `CONSTITUTION.md` §5 skill composition table and `.claude/skills/README.md`
  composition table: add `hexagonal-arch` under the Architecture decisions row
  — it was used for the real, merged `ADR-021` payment pilot but was missing
  from both canonical catalogs.

No code, tests, or runtime behavior changes.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The JaCoCo coverage gate enforced at `mvn verify` is a 70% line / 25% branch ratchet floor, not 80% | `.claude/rules/code-quality.md` (existing, authoritative) | Made explicit in the two rule files that previously contradicted it |
| Skills actually exercised by a merged change must be discoverable from the canonical skill composition tables | CONSTITUTION.md §5 / `.claude/skills/README.md` (existing intent) | Made explicit — `hexagonal-arch` was a silent gap |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — this is a documentation/governance-layer correction with no
spec-level (application) behavior change. `skip_specs: true` is set in
`.openspec.yaml`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | already removed; only a stale doc reference to it is fixed |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing architecture; this is a documentation-consistency fix, not a
structural change. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `AGENTS.md` | Coverage comment on `mvn jacoco:check` corrected to state the enforced ratchet floor |
| `.claude/rules/ai-agent-workflow.md` | Two "Coverage ≥ 80%" gate/checklist lines reworded to floor-vs-target language |
| `.claude/skills/maven-build/SKILL.md` | Remove `frontend-swing` command and catalog row |
| `CONSTITUTION.md` | §5 skill composition table gains a `hexagonal-arch` reference under Architecture decisions |
| `.claude/skills/README.md` | Composition table gains a matching `hexagonal-arch` reference |
| `CHANGELOG.md` | n/a — not user-visible (internal governance docs only) |

## Out of Scope

- The broader preflight.sh-vs-CI gate audit is tracked separately under
  issue/PR #1029/#1030 and is not duplicated here.
- `drawio` and `motion-design` skills, found unreferenced by any governance
  doc during the audit, are left as-is: they are generic, low-risk, and
  adding them to the composition table is a separate judgment call about
  scope, not a correctness fix. Noted in the traceability ledger for
  follow-up triage.
- No changes to `CONSTITUTION.md`'s process content (§1-§11) — only the §5
  skill table gains one row.
