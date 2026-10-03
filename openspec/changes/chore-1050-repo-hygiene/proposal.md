# Repo hygiene — gitignore, CODEOWNERS, large binaries, history ADR

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1050 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/chore-1050-repo-hygiene-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue `#1042→#1041→#1040→#1043→#1045→#1056→#1055` |

## Objetivo

Repository hygiene from the 2026-09 production-readiness audit: a global
`*.txt` gitignore rule blocks legitimate text assets, `.serena/` tooling files
are tracked without an ignore policy, a 13 MB user-manual PDF bloats the tree,
and git history still carries large obsolete binaries (~120 MB pack). Narrow
ignore rules, align ownership paths, relocate the large PDF, and record an ADR
on whether to rewrite history with `git filter-repo`.

## What Changes

- Narrow `.gitignore` so `*.txt` is not a global ban; keep ignoring local noise
  while allowing tracked text assets (e.g. `requirements.txt` under testing).
- Add `.serena/` to `.gitignore` and stop tracking the currently committed
  Serena project files (keep them local-only).
- Verify `.github/CODEOWNERS` paths match the current tree (note: `/frontend-swing/`
  was already removed under #1046 — confirm no stale paths remain).
- Move the 13 MB `Manual de Usuario Notaire.doc.pdf` out of normal git blobs
  (Git LFS and/or GitHub Release asset) and document how operators obtain it.
- Add ADR under `docs/200-architecture/202-ADR/` deciding whether/when to run
  `git filter-repo` (or equivalent) for historical blobs (81 MB `.doc`, `.eap`,
  `notaire.jar`, committed `scripts/doc2md/venv`, etc.). **This change records
  the decision; executing a history rewrite is out of scope unless the ADR
  explicitly schedules a follow-up issue.**
- Document the hygiene policy in permanent devops/development docs + CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Needed text assets MUST remain trackable; global `*.txt` ignore MUST NOT block them | CU76; #1050 AC | Changed (gitignore) |
| Local AI/tooling state under `.serena/` MUST NOT be committed | CU76; #1050 AC | New (ignore + untrack) |
| CODEOWNERS paths MUST match the live module tree (no removed `frontend-swing`) | CU76; #1050 AC; #1046 | Made explicit (verify) |
| Large binary manuals MUST NOT live as ordinary git blobs | CU76; #1050 AC; related #585/#682 | Changed (LFS/releases) |
| History rewrite with `git filter-repo` MUST be an explicit ADR decision before any force-push | CU76; #1050 AC; Constitution §9/§12 | New (ADR) |

## Capabilities

### New Capabilities

- `repo-hygiene`: ignore rules, CODEOWNERS tree fidelity, large-PDF relocation,
  and ADR-governed history-rewrite decision for repository size hygiene.

### Modified Capabilities

- (none under `openspec/specs/` today cover repo hygiene ignore/LFS/ADR policy)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Already removed; CODEOWNERS must not reference it |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | maybe | Only if LFS smudge/filter or release upload needs CI wiring |
| Repo root / docs | yes | `.gitignore`, `.github/CODEOWNERS`, manuals path, ADR, CHANGELOG |
| Scripts | maybe | Optional helper to publish/fetch the PDF from Releases |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: optional `git-lfs` for clone machines if LFS chosen
- **BREAKING**: none for API clients; clones may need LFS if PDF stays in-tree via LFS

### Architecture review

Follows existing docs layout and ADR process. Requires **new ADR**
(e.g. `ADR-022-git-history-rewrite-and-large-binaries.md`) for the
`filter-repo` decision and LFS vs Release choice for the user manual PDF.
Does not alter application architecture.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-022-…` (new) | Decision: filter-repo now/later/never; LFS vs Releases for manuals |
| `docs/200-architecture/202-ADR/README.md` | Index the new ADR |
| CU76 permanent UC doc (if it lists QA infra artifacts) | Note repo-hygiene / binary policy pointer |
| Dev/devops contributor docs (README or `docs/300-development/…`) | How to obtain the user manual; `.serena/` local-only; gitignore txt policy |
| `CHANGELOG.md` | chore entry for hygiene |

## Out of Scope

- Executing `git filter-repo` / force-pushing rewritten history (unless ADR
  schedules a **separate** issue after stakeholder approval).
- Full cleanup of every historical blob listed in #585 / #682 (related; may be
  referenced by the ADR as follow-ups).
- Product UI, backend APIs, Flyway, or frontend features.
- Implementing #1058 / #976.
