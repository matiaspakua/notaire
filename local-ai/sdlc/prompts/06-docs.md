# Phase 6 — Permanent documentation for #{{ISSUE}} (Gate 3)

Read the "Documentation Impact" section of `openspec/changes/{{CHANGE}}/proposal.md`.

1. Update every permanent document listed there (`docs/...`, `README.md`,
   `AGENTS.md`, `.claude/rules/...`). Edit the single place the fact lives;
   never copy text between documents. Move obsolete docs to `docs/000-archive/`.
2. If the change is user-visible (TYPE feat/fix, visible behaviour, or the issue
   is labelled `security` — operators must know about removed credentials), add one
   line under `## [Unreleased]` in `CHANGELOG.md` in the right subsection
   (Added / Changed / Fixed / Removed / Security) ending with `(#{{ISSUE}})`.
3. If API_CHANGE=yes: make sure the controller's OpenAPI annotations and any
   `backend-api/api-test/` Bruno request match the new contract.
4. `tasks.md`: change `- [ ]` to `- [x]` on the group 8 items you actually did,
   with the edit tool, one block per item. Nothing else in `tasks.md`: the harness
   rejects any other change. Leave the preflight item unticked.
   `traceability.md`: fix only content rows that are wrong (e.g. a Planned File that
   does not exist), one edit block per row. Never touch the Commits or CHANGELOG
   rows — the harness records SHAs itself after this phase.
5. Run `bash scripts/validate-sdlc-plan.sh {{CHANGE}}` — must pass.
   Do not touch code or tests in this phase.
6. Commit: `docs(<scope>): <what> ` with body `Refs #{{ISSUE}}`.
