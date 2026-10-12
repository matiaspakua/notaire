# Development documentation

Operational guides for developers: environment setup, code standards, and the
testing strategy. They complement `CLAUDE.md` and `CONSTITUTION.md` (repo root),
which define the mandatory workflow.

## Structure

| Folder / file | Contents |
|---------|-----------|
| [`DEVELOPMENT-PLAN.md`](DEVELOPMENT-PLAN.md) | SDLC process, conventions, and the architecture migration roadmap (synthesis document) |
| [`303-testing/TEST-PLAN.md`](303-testing/TEST-PLAN.md) | Master test plan: test levels, catalog by use case, reporting |
| [`DEPLOYMENT-PLAN.md`](DEPLOYMENT-PLAN.md) | Environments, release and promotion process, rollback, and the release checklist |
| [`301-setup/`](301-setup/) | Installation, module layout, and development commands (backend and frontend) |
| [`302-code-standards/`](302-code-standards/) | Code standards: DTO mapping, error handling, JPA lazy loading, Spring transactions |
| [`303-testing/`](303-testing/) | Test suites, the use-case-to-API matrix, and QA guides |
| [`templates/`](templates/) | Templates for OpenSpec specifications |
| [`ASSESSMENT-2026-10.md`](ASSESSMENT-2026-10.md) | System assessment (October 2026): test results, findings by dimension, and linked issues |
| [`REPO-SPLIT-PLAN.md`](REPO-SPLIT-PLAN.md) | Phased plan and exit criteria for the repository topology (#1197, ADR-024) |
| [`MODULE-OWNERSHIP.md`](MODULE-OWNERSHIP.md) | In-repo module map (ADR-026): paths, fleets, verify, Mermaid deps — #1197 Phase 0 |
| [`REPO-METRICS-BASELINE.md`](REPO-METRICS-BASELINE.md) | Reproducible repo metrics for topology decisions (#1256 / #1417) |
| [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md) | Map of local checks (`workspace/sdlc/preflight.sh`) to CI jobs |
| [`304-ai-sdlc-cloud/`](304-ai-sdlc-cloud/) | Cursor Cloud AI SDLC fleet: foreman, specialists, env checklist, [CI merge gate](304-ai-sdlc-cloud/CI-MERGE-GATE.md), validation (not `local-ai/`). Agents pick by the lanes in [project management](../github/PROJECT-MANAGEMENT.md) |
| [CICLO-DE-VIDA.md](../200-architecture/203-design/CICLO-DE-VIDA.md) | Permanent domain model. A change that moves a state updates this file in the same PR |
| [`INGENIERIA.md`](INGENIERIA.md) | Artifact IDs, forward/back trace, and process boxes. Method from the specialization guide, fitted to this repo |
| [`EVALUACION-INGENIERIA.md`](EVALUACION-INGENIERIA.md) | Which engineering documents exist, and which gaps are not a missing file |

OpenSpec and the Constitution: see [`docs/openspec/NOTAIRE-ADAPTATIONS.md`](../openspec/NOTAIRE-ADAPTATIONS.md)
(SpecKit, removed in #1083: [`docs/000-archive/speckit/`](../000-archive/speckit/)).

## Before changing code

1. Read [`CONSTITUTION.md`](../../CONSTITUTION.md) and
   [`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md) — the
   mandatory flow (issue + use case → branch → TDD → tests → commit → pull request).
2. When a change moves a domain state, update
   [CICLO-DE-VIDA.md](../200-architecture/203-design/CICLO-DE-VIDA.md). The artifact
   and traceability method is in [`INGENIERIA.md`](INGENIERIA.md).
3. Run `bash workspace/sdlc/preflight.sh` before every push (see
   [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md); `mvn verify` alone is not enough).

## Navigation

- [← Documentation](../)
- [Business](../100-business/)
- [Architecture](../200-architecture/)
