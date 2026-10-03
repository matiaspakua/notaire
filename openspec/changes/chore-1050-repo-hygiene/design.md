> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1050 (audit-2026-09, DEVOPS, chore, priority:medium, CU76). Verified on
`origin/main` tip `9642a033` (2026-10-03):

| Location | Finding |
|----------|---------|
| `.gitignore:40` | Global `*.txt` still present; `git check-ignore` would block `testing/e2e-swing/requirements.txt` |
| `.serena/` | Present and **tracked** (`project.yml`, memories, …) — issue text said “untracked”; treat as “must not be in git” → ignore + `git rm --cached` |
| `.github/CODEOWNERS` | Already points at `/frontend/` only; comment notes Swing removed under #1046 — **verify** still accurate, no stale paths |
| Manual PDF | `docs/100-business/105-manuals/C_Manual de Usuario/Manual de Usuario Notaire.doc.pdf` ≈ **13 MB** tracked |
| Pack size | `size-pack` ≈ **120 MiB**; history still holds large `.doc` / `.eap` / `notaire.jar` / past `venv` blobs (related #585/#682) |

Implement **after** the coordinator queue
`#1042 → #1041 → #1040 → #1043 → #1045 → #1056 → #1055` so hygiene does not
collide with in-flight CD/frontend PRs.

## Goals / Non-Goals

**Goals:**

- Precise ignore rules for text assets + Serena.
- CODEOWNERS fidelity to the live tree.
- User-manual PDF out of ordinary blob storage.
- Written ADR deciding history rewrite posture.

**Non-Goals:**

- Force-pushing rewritten `main` in the same PR as ignore/CODEOWNERS/PDF moves
  unless a separate approved follow-up says so.
- Deleting all historical binaries from every clone’s object store without ADR.
- Product feature work (#1058, #976).

## Decisions

1. **Replace global `*.txt` with targeted ignores**
   - Prefer ignoring known noisy paths (`*.log` already; IDE dumps; local notes)
     rather than all text files.
   - Explicitly allow `requirements.txt`, `*.txt` under `docs/` when needed, and
     testing trees.
   - Alternative rejected: keep `*.txt` + endless `!` exceptions — fragile.

2. **`.serena/` local-only**
   - Add `.serena/` to `.gitignore`; `git rm -r --cached .serena`.
   - Do not recreate Swing or commit agent memories.

3. **CODEOWNERS verify-only unless drift found**
   - On `9642a033` Swing path is already gone. Implement task is assert + fix
     any other dead paths discovered at implement time.

4. **PDF: prefer GitHub Release asset + short stub in docs; LFS acceptable**
   - Primary recommendation: publish the PDF on a Documentation/manuals Release
     (or attach to an existing docs release) and replace the in-tree file with a
     markdown stub linking to the download URL / `gh release download` instructions.
   - LFS is acceptable if the team wants the path to remain in-tree; then add
     `.gitattributes` LFS rule and migrate the blob.
   - Choose one approach in the ADR; do not leave both half-done.

5. **ADR-022 for filter-repo**
   - Default recommendation for **this** change: **defer rewrite** to a follow-up
     issue after LFS/Release cleanup lands, because rewriting shared history
     breaks open PRs and Cloud Agent worktrees.
   - ADR must list candidate blob paths and risks; related #585/#682 stay open
     until that follow-up.

## Riesgos / Trade-offs

- [Untracking `.serena`] → Contributors lose shared memories in git — acceptable;
  Serena is local tooling. Mitigation: note in contributor docs.
- [Removing PDF from tree] → Offline docs clones miss the PDF — Mitigation:
  stub + Release instructions.
- [History rewrite later] → Force-push pain — Mitigation: ADR defers; never
  rewrite in a silent chore.
- [LFS not installed on agent VMs] → Mitigation: prefer Release asset over LFS
  unless install is standardized in `.cursor/install.sh`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Needed requirements.txt not ignored | unit (static) | `scripts/test_repo_hygiene.py` (or equivalent) |
| `.serena` ignored + untracked | unit (static) | same |
| CODEOWNERS no frontend-swing | unit (static) | same |
| PDF not ordinary 13 MB blob | unit (static) | same (size/pointer/absence) |
| ADR documents filter-repo decision | docs checklist / file exists assert | same or PR checklist |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java)
- New integration tests: n/a
- Coverage impact (JaCoCo): none expected

TDD: write the static asserts first against pre-change tree, observe fail, then
apply ignore/CODEOWNERS/PDF/ADR fixes until green.

## Regression Strategy

- Existing tests affected: none expected for app suites; ensure CI still
  checkouts without requiring LFS if Release strategy chosen.
- Full suite command: `mvn verify -pl backend-api` (sanity); `bash scripts/preflight.sh`
- HTTP/Bruno: n/a
- Legacy paths at risk: do not resurrect `frontend-swing`

## Playwright Strategy

- n/a — no UI surface (repo hygiene / docs / git metadata only).
- PR must still pass required CI including Playwright job if branch triggers it;
  no product E2E edits expected.

## Deployment Strategy

- Merge via PR to `main` after heavy CI green.
- No runtime deploy behavior change.
- If Release asset used for PDF, publish the asset before or in the same change
  window and link the stub.

## Rollback Strategy

- Revert the PR. If LFS migration was used, reverting restores the prior blob
  pointer state; if Release-only, re-add PDF from Release in a follow-up revert
  commit. History rewrite (if ever done later) needs its own rollback plan in
  that follow-up’s ADR — not this PR.
