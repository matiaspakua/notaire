# SpecKit vs OpenSpec: Evaluation

> Governed by [`speckit/NOTAIRE-ADAPTATIONS.md`](NOTAIRE-ADAPTATIONS.md) and
> [`openspec/NOTAIRE-ADAPTATIONS.md`](../openspec/NOTAIRE-ADAPTATIONS.md).
> Written after all 3 evidence features (CU03/#860, CU10/#863, CU43/#865)
> went through the SpecKit-adapted flow end to end — grounded in what was
> actually built, not a feature-list comparison of the two tools in the
> abstract.

## Verdict

Both tools reach Constitution compliance, but only because both needed the
same three-piece project-owned override (Issue+Use-Case linkage, SDLC
strategy sections, a mechanical live-Issue gate) — neither ships it. Where
they differ is in the shape of that override and in how much of each tool
survives an upgrade unmodified. **OpenSpec required a smaller diff** to
reach parity (it already had `design.md` and a proposal header format close
to what the Constitution needs); **SpecKit's native spec/plan/tasks split
by user story reads better for a multi-story feature** like CU10, at the
cost of a heavier `tasks.md` template. Notaire keeps both, unintegrated, as
the plan required — this is not a recommendation to drop either.

## What worked out of the box (no override needed)

- **Both tools' TDD framing matches the Constitution directly.** SpecKit's
  native "Tests before implementation" phase and OpenSpec's Gate 2 map
  1:1 onto `.claude/rules/ai-agent-workflow.md` Step 2 — no adaptation
  needed here, just wrapping.
- **SpecKit's Given/When/Then scenario format was already present** in the
  vendor `spec-template.md`, unlike OpenSpec's packaged `spec-driven` schema
  which needed the Acceptance Criteria section added explicitly. One fewer
  override on the SpecKit side.
- **SpecKit's per-user-story task organization** (`Phase 3: User Story 1`,
  `Phase 4: User Story 2`, ...) worked unmodified for CU10's three
  independent user stories (list, register movimiento, auto-transition) —
  no project-owned change to that structure was needed, only the Notaire
  SDLC Gates wrapped around it.
- **`gh issue view`-based live verification** (the actual mechanical fix in
  both frameworks) is identical bash in both scripts — this piece is
  tool-agnostic and cost nothing extra to duplicate.

## What required project-owned overrides (both tools, same shape)

| Gap | OpenSpec fix | SpecKit fix |
|---|---|---|
| No mandatory Issue/Use-Case field | `notaire-sdlc` schema `proposal.md` template | `spec-template.md` Notaire Traceability header |
| No live Issue verification | `scripts/validate-sdlc-plan.sh` `gh issue view` | `scripts/validate-speckit-plan.sh` `gh issue view` (same code) |
| No release-aware plan | `design.md` mandatory (upstream OpenSpec: conditional) | `plan.md` gained 5 SDLC strategy sections (upstream SpecKit: architecture-only) |
| No traceability ledger | new `traceability` artifact in the schema | new `traceability-template.md` (SpecKit has no native equivalent at all) |
| No SDLC gates in the task list | `tasks` template's 12 mandatory groups | Notaire SDLC Gates block wrapped around SpecKit's native per-story phases |

Full detail for each side: [`openspec/NOTAIRE-ADAPTATIONS.md`](../openspec/NOTAIRE-ADAPTATIONS.md#the-fix-four-project-owned-pieces-no-vendor-file-assumed-stable),
[`speckit/NOTAIRE-ADAPTATIONS.md`](NOTAIRE-ADAPTATIONS.md#the-fix-three-project-owned-pieces-no-vendor-file-assumed-stable).

## Friction found only after running the flow for real

The original adaptation plan (Phase 2 of the tooling change) did not
anticipate two problems that only surfaced once a SpecKit feature actually
merged:

- **Archive-on-merge is not self-enforcing.** `scripts/validate-speckit-plan.sh`
  requires every `speckit/specs/*/` feature's Issue to still be `OPEN`. The
  first merge (CU03/#860) broke every subsequent push repo-wide the moment
  its Issue closed (#866 → PR #867, fixed by moving the directory to
  `speckit/specs/archive/`, which the validator skips). The *identical*
  failure then recurred verbatim for CU10/#863 the moment *its* PR merged
  (#868 → PR #869), because moving the directory was a manual step nobody
  was forced to take. The real fix was not the archive mechanism itself
  (OpenSpec's `openspec/changes/archive/` needed no such correction — its
  archive skill is a single command an agent is instructed to run at Gate 5)
  but making the move an explicit, generated Gate 5 task in
  `tasks-template.md` for every future feature, so it can't be silently
  skipped. **This is the one adjustment SpecKit needed that OpenSpec did
  not** — OpenSpec's archive step was already a first-class CLI operation
  (`openspec archive <name>`) with its own vendor skill instructing when to
  run it; SpecKit has no archive concept at all, so Notaire had to invent
  one from scratch, and the first version of that invention wasn't
  self-enforcing.
- **H2-vs-PostgreSQL schema drift bit CU10, not CU03 or CU43's design.**
  Two real bugs (`DocumentoPresentadoController.toEntity()` leaving
  `liberado`/`observado`/`reingresado` unset — valid under H2's
  `ddl-auto=create`, a NOT NULL violation on real PostgreSQL; a movimiento
  dialog missing `max-h`/`overflow-y-auto`) were caught only by running
  Playwright against the live dev stack, not by the H2-backed unit/
  integration suite. This is a pre-existing project-wide risk
  (`.claude/rules/database-migrations.md`'s Flyway-is-truth rule exists
  because of it), not something either spec-driven tool introduced or
  prevented — noted here because CU43's own regression suite
  (`GestionReingresoDocumentacionPgIntegrationTest`) was written specifically
  in response to this CU10 finding, confirming the lesson transferred
  across features within the same session.
- **A transaction-propagation fix attempt regressed a different feature.**
  CU10's US3 auto-transition first tried `Propagation.REQUIRES_NEW` on
  `GestionTransitionService.transicionar`, which broke
  `GestionArchiveDebtService.archivar`'s existing flow; the revert and the
  restructured call site are recorded in CU10's `traceability.md` per P4
  rather than hidden. Neither framework's template would have caught this on
  its own — it surfaced because the full regression suite (Step 5) ran
  before commit, which both frameworks' SDLC-gate wrapping requires.

## Deferred, not silently dropped

- **`speckit/.specify/memory/constitution.md` and the template deltas are
  vendor-owned files** that `specify upgrade` will reset; the only
  documented mitigation today is "re-apply from git history after upgrade"
  (see `speckit/NOTAIRE-ADAPTATIONS.md` point 1). No automated re-apply
  script exists yet. OpenSpec's equivalent (`openspec/config.yaml`) is a
  CLI-native extension point immune to this problem, which is a structural
  advantage OpenSpec has that this evaluation does not attempt to close on
  the SpecKit side — it would require SpecKit itself to add an extension
  point, which is out of Notaire's control.
- **No CI job wires `scripts/validate-speckit-plan.sh` into
  `.github/workflows/pr-validation.yml`'s job graph the same way the
  `sdlc-plan` job does for OpenSpec** — `scripts/preflight.sh` runs both
  scripts locally and in the pre-push hook, which is the gate that actually
  blocks a push, but the CI-side visibility (a named, separately reported
  job) is asymmetric between the two tools today. Left as a follow-up, not
  required by #870's acceptance criteria.
- **The #868 self-enforcement fix did not actually reach CU43.** PR #871
  merged (2026-08-29), Issue #865 closed, but `speckit/specs/
  003-cu43-reingresar-documentacion/tasks.md` — authored before the fix
  landed — carried no generated Gate 5 archive task, so `main` broke the
  pre-push hook again the same way CU03 and CU10 did, requiring a third
  manual fix (Issue #873, mirroring #866/#867). The fix in #868 only patches
  `tasks-template.md` for *future* `/speckit.tasks` runs; it does nothing for
  specs already scaffolded before that point, which CU43's was. The durable
  lesson: a template fix only closes a gap for artifacts generated after it
  ships — retrofitting in-flight specs needs an explicit, separate pass, and
  the mechanical gate (`validate-speckit-plan.sh` failing loudly on `main`)
  is what actually caught this, not the template.

## Evidence

See `speckit/NOTAIRE-ADAPTATIONS.md`'s Evidence section for the full
Issue/PR table and `speckit/specs/archive/00{1,2}-*/traceability.md` for
the two merged features' complete Gate logs, including the real bugs and
the reverted approach recorded above.
