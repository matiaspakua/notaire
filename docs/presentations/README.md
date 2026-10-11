# Notaire Presentations

## SDD Framework Guide

**File:** `sdd-framework-guide.html`

A presentation on Spec-Driven Development, comparing OpenSpec with GitHub Spec Kit,
with two demos based on changes **already merged** in Notaire.

### Contents (23 slides)

1. Title
2. **SDD foundations** — what it is, without SDD versus with SDD
3. **Problems it solves** — alignment, traceability, consistency, context tax
4. **OpenSpec vs Spec Kit** — philosophies
5. **Comparison table** — detailed characteristics
6-7. **Case A — Brownfield with OpenSpec** — the real flow (`/opsx:explore → propose → apply → verify → archive`, Mermaid diagram) plus delta specs and the advantages on legacy code
8-9. **Case B — Greenfield with Spec Kit** — the flow (Mermaid diagram) plus the artifact layout
9.3. **Why SDD needs adjustments in an enterprise or legacy setting** — Jira, GitHub, Confluence, dev environments, compliance
9.5. **What is Notaire?** — simplified technical context (Mermaid diagram based on SAD §3.2 Technical Context) before the demos
10-12. **DEMO 1 — real OpenSpec**: Issue #879 → PR #882 (fix) → PR #884 (archive Gate 5) — fix of `Property.valuacionFiscal` (slide 12 links the issue, branch, proposal/design/tasks/spec, commit, test, and both PRs)
13-15. **DEMO 2 — real Spec Kit**: Issue #865 (CU-43) → PR #871 (feat) → PR #874 (archive Gate 5) — Re-file documentation
15.5. **Demo 2 — artifacts with links**: issue, branch, spec/plan/tasks/traceability, commit, E2E test, and both PRs
16. **Close** — when to use each framework
17. **Adoption in teams** — Pilot → Standards → Rollout
18. **Final close**
19. **Appendix A — Testing with SDD** — the spec as a verifiable quality engine; Mermaid diagram of the OpenSpec workflow adapted to Gherkin/BDD, TDD, Cucumber, and contract testing with Pact/Testcontainers
20. **Appendix B — Cybersecurity with SDD** — the spec as a chain of control and evidence; Mermaid diagram of the OpenSpec workflow adapted to STRIDE threat modeling, SAST/SCA/SBOM, OWASP ZAP, and policy-as-code with OPA

### Demos grounded in real repository artifacts

- **OpenSpec**: `openspec/changes/archive/2026-08-31-fix-inmueble-valuacion-fiscal-type/`
- **Spec Kit**: `docs/000-archive/speckit/specs/archive/003-cu43-reingresar-documentacion/`

Every issue number, PR number, and test result cited in the presentation
comes from those artifacts and the commit history. They are not invented examples.

### Characteristics

- **Notaire theme (Apple design tokens)** — dark/light mode:
  - Primary Blue (`#0A84FF` / `#0071E3`) — Spec Kit identity
  - Success Green (`#34C759`) — OpenSpec identity
  - Warning Orange (`#FF9500`) — Gates / Quality
  - Error Red (`#FF453A`) — breaking changes / risk
- **Embedded Mermaid diagrams** (OpenSpec and Spec Kit flows, Notaire technical context, and OpenSpec workflows adapted to testing and cybersecurity, rendered live)
- **Motion** with GSAP (slide entrance, cursor glow, 200–400ms transitions)
- **Interaction:** ← → keyboard navigation, buttons, slide counter, F fullscreen, T toggles the theme

### How to open it

```bash
open docs/presentations/sdd-framework-guide.html
```

Or with a local server (recommended so Mermaid and fonts load without CORS limits):

```bash
python -m http.server 8000
# http://localhost:8000/docs/presentations/sdd-framework-guide.html
```

**Navigation:** `← →` keyboard · click Previous/Next · `F` fullscreen · `T` theme

### References

- Real OpenSpec demo: `openspec/changes/archive/2026-08-31-fix-inmueble-valuacion-fiscal-type/`
- Real Spec Kit demo: `docs/000-archive/speckit/specs/archive/003-cu43-reingresar-documentacion/`
- Constitution: `CONSTITUTION.md` §5 (SDLC Workflow), §6 (Quality Gates)

---

**Last updated:** 2026-08-31 | **Branch:** `docs/redesign-sdd-presentation`
