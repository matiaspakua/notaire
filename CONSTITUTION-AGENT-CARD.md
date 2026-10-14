# Constitution — Agent Card

> Digest of [`CONSTITUTION.md`](CONSTITUTION.md). **The full Constitution prevails.**
> Load the full file for Gate details, exceptions, and release rules.

## Non-negotiables

1. **Issue + Use Case** before any change (CU-XX / RF-XX).
2. **OpenSpec Gate 1** before implementation (`cd docs` → `openspec` / `validate-sdlc-plan.sh`).
3. **TDD**: write failing tests first; observe the failure; then implement.
4. **Branch** `<type>/<#>_desc` from updated `main`; never commit to `main`.
5. **Quality gates absolute**: unit + integration + coverage ratchet; Playwright for UI;
   local security cases via `python3 security/run_gates.py --profile local` inside
   `bash workspace/sdlc/run_pipeline.sh`; `bash workspace/sdlc/preflight.sh` before push;
   heavy CI via `check-heavy-ci.sh` before merge.
   **Foreman merge rule (#1624):** the Foreman merges Worker PRs only when all
   required checks are green + `check-heavy-ci.sh` exits 0; the Owner merges
   Constitution, `sdlc-exception` and harness PRs; Workers never merge.
11. **Archive in the PR (#1623):** a PR archives the OpenSpec change it implements
   (`openspec archive <c> --yes`) unless the change sets `multi_pr: true`.
6. **Flyway** is schema source of truth; never edit applied migrations.
7. **Docs are part of Done**; no secrets in git; design-system tokens on frontend.
8. **KISS** and bounded **Boy Scout** (CONSTITUTION §2 P2/P11): simplest fix; clean only what you touch in the same PR.
9. **Path-scoped CI (#1257)**: docs-only PRs skip Java/E2E leaves; aggregators accept `skipped`;
   `main` always full suite.
10. **Board coherence (#1550)**: CLOSED⇒Done+no WIP labels; OPEN WIP⇒In Progress+`in-progress`;
   blocked⇒`blocked` label (no Blocked column). Validate:
   `python3 workspace/sdlc/check-board-status-label-coherence.py` (skips without `gh` auth).

12. **Continuous publication (#1681):** push the branch immediately after creating it; after each
   group of atomic commits push and comment on the Issue (stage, last commit, what remains) with
   `bash workspace/sdlc/publish-progress.sh <issue> "<stage>"`. GitHub always holds the current state.

## Workflow (abbrev)

Issue → OpenSpec → branch → TDD → implement → refactor → all tests → commit → push →
docs → PR → merge → smoke.

## Tooling pointers

| Need | Where |
|------|-------|
| Full process | `CONSTITUTION.md` |
| Ops workflow | `.claude/rules/ai-agent-workflow.md` |
| Modules | `python3 workspace/modules.py …` / `workspace/modules.yaml` |
| OpenSpec | `docs/openspec/` (run CLI from `docs/`) |
| Preflight | `bash workspace/sdlc/preflight.sh` |
| Board Status/labels | `python3 workspace/sdlc/check-board-status-label-coherence.py` |
| Skills catalog | `.claude/skills/README.md` (load on demand) |

*Keep this card ≤3 KB. Expand detail in CONSTITUTION.md, not here.*
