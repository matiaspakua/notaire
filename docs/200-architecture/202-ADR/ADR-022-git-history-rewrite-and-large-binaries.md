# ADR-022: Git History Rewrite and Large Binaries

## Status

Accepted — 2026-10-03 (issue #1050)

**Decision summary:** relocate the in-tree user-manual PDF to a GitHub Release
asset (not Git LFS); **defer** `git filter-repo` history rewrite to a follow-up
issue after open PRs / Cloud Agent worktrees settle.

## Context

The 2026-09 production-readiness audit (#1050, CU76) found:

| Finding | Detail |
|---------|--------|
| Global `*.txt` in `.gitignore` | Blocked legitimate assets such as `testing/e2e-swing/requirements.txt` |
| `.serena/` | Local AI tooling state was tracked |
| User-manual PDF | `Manual de Usuario Notaire.doc.pdf` ≈ 13 MB as a normal blob |
| Pack size | `git count-objects` ≈ **120 MiB** `size-pack` |
| Historical heavy blobs | Related #585 / #682 — past `.doc` (~81 MB), `.eap` models, `notaire.jar`, committed `scripts/doc2md/venv`, etc. |

Narrowing ignore rules and untracking `.serena/` is low-risk. Removing the
13 MB PDF from ordinary blob storage and deciding whether to rewrite shared
history are architectural choices that affect every clone and open PR.

## Decision

### 1. User-manual PDF → GitHub Release (not LFS)

- Remove `docs/100-business/105-manuals/C_Manual de Usuario/Manual de Usuario Notaire.doc.pdf`
  from the git index.
- Publish it as release tag **`docs-manuals`**, asset
  **`Manual-de-Usuario-Notaire.doc.pdf`**.
- Keep a markdown stub + `docs/tools/fetch-user-manual.sh` so operators can
  download the binary without bloating clones.
- Ignore the downloaded PDF path locally so it is not re-committed.

**Why not Git LFS for this change:** Cloud Agent / contributor VMs are not
uniformly configured for LFS smudge on every clone; a Release asset + `gh`
fetch keeps CI checkouts LFS-free while still documenting how to obtain the
PDF. LFS remains acceptable later if the team standardizes `git-lfs` in
`.cursor/install.sh` — that would be a follow-up ADR amendment, not a silent
switch.

### 2. History rewrite with `git filter-repo` → **defer**

Do **not** rewrite `main` in the #1050 PR.

| Option | Verdict |
|--------|---------|
| Rewrite now in this PR | Rejected — force-push breaks open PRs, Cloud Agent worktrees, and fork clones |
| Never rewrite | Rejected — pack remains ~120 MiB with obsolete binaries forever |
| **Defer to follow-up** | **Chosen** — land ignore/CODEOWNERS/PDF Release hygiene first; schedule rewrite under related #585 / #682 (or a dedicated issue) after stakeholder approval |

The follow-up MUST:

1. List exact blob paths / sizes in scope (manual PDF history, large `.doc`,
   `.eap`, `notaire.jar`, past `venv`, etc.).
2. Coordinate a maintenance window and require all contributors to re-clone
   (or carefully re-fetch) after the force-push.
3. Update this ADR status when the rewrite ships or is cancelled.

## Options Considered

- **Keep the 13 MB PDF as a normal blob.** Rejected: every clone pays the cost;
  contradicts the audit AC.
- **Migrate the PDF to Git LFS in this PR.** Acceptable technically, but deferred
  in favour of Release assets so CI/agent clones stay simple (see Decision §1).
- **Run `git filter-repo` immediately.** Rejected for shared-history risk while
  the SDLC cloud fleet has active branches (see Decision §2).
- **Ignore-only / docs-only without ADR.** Rejected: Constitution requires an
  ADR before history-altering DevOps decisions.

## Consequences

### Positive

- Fresh clones no longer fetch the 13 MB PDF as a tip blob.
- Ignore rules allow needed `*.txt` assets; Serena stays local.
- Rewrite risks are explicit and gated by a future issue.

### Negative / follow-ups

- Historical objects remain in the pack until the deferred rewrite.
- Operators need `gh` (or a browser) to download the PDF once.
- Someone with release write access must publish the `docs-manuals` asset if it
  is not already present.

## Pending Owner decision (#1261 / #1197 P0.6)

The deferred history rewrite (Decision §2) and the largest tip-tree weight outside
`docs/` are now tracked as issue **#1261**. Measured tip weight (2026-10-10,
`workspace/ci/repo-metrics.py`): `deprecated/` ≈ **13.4 MB** / **767 files**;
`docs/` ≈ 20 MB; always-loaded agent context ≈ 2k tokens (post-#1259).

**Agents must not delete `deprecated/` or run `git filter-repo` until the Owner
records a choice here.**

> **Note:** GitHub may show issue #1261 as closed after the packaging PR (#1429).
> That PR only documented Option A/B/C — it did **not** execute a decision.
> The Owner must still pick an option (and reopen #1261 or open a follow-up issue
> if the tracker entry must stay open).

| Option | Tip tree | History | Notes |
|--------|----------|---------|-------|
| **Option A** | Tag `archive-monorepo-pre-split`, remove `deprecated/` from tip | Keep historical blobs | Preferred size win without SHA churn |
| **Option B** | Option A + `git filter-repo` purge | Rewrites SHAs; force-push `main` | Only with freeze window + re-clone notice (Decision §2 follow-up MUST list) |
| **Option C** | Keep `deprecated/` | No change | Explicitly accept pack / tree cost |

### Acceptance (when Owner chooses)

1. Record the chosen option and date in this section (replace “Pending” with the decision).
2. Capture before/after metrics via `python3 workspace/ci/repo-metrics.py --markdown …`.
3. If A or B: one PR updates the tree, `test_repo_hygiene.py` guards, MODULE docs, and this ADR.

## Related

- Issue #1050 (this change), CU76
- Owner follow-up: #1261 (#1197 P0.6), ADR-024
- Related cleanup: #585, #682
- CODEOWNERS Swing removal already done under #1046 (verify-only here)
- Spec: `docs/openspec/changes/chore-1050-repo-hygiene/`
- Spec (decision packaging): `docs/openspec/changes/chore-1261-deprecated-owner-decision/`
