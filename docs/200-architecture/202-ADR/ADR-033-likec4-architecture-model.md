# ADR-033: LikeC4 as the architecture model

**Status:** Accepted — 2026-11-24
**Date:** 2026-11-24
**Deciders:** Owner direction via issue #2483 (CU76)
**Related:** ADR-027 (amended), ADR-030 (hexagon), ADR-026 (modules), ADR-022 (no large binaries),
[OpenSpec change](../../openspec/changes/archive/2026-11-24-docs-2483-likec4-architecture-model/proposal.md)

## Context

ADR-027 made Mermaid the language of active diagrams. Mermaid draws well, but each diagram is drawn on its own:
the SAD kept nine PlantUML fences, the system context on GitHub Pages was a separate hand-written chart, and the
two drifted from the code. In November 2026 the SAD still showed the retired Swing client and the deleted `jpa`
and `service` packages, and said the frontend had no ports.

[LikeC4](https://likec4.dev) describes a C4 model once (elements, relationships, deployment) and derives views
from it: element views, deployment views and dynamic (flow or sequence) views. It has a CLI that validates the
model, builds an interactive static site and exposes the computed views through an API.

## Decision

1. **The LikeC4 model in [`210-architecture-model/`](../210-architecture-model/README.md) is the source of the
   architecture views**: context, containers, the backend and frontend hexagons, security controls, testing and
   delivery, and the deployment of every environment.
2. The model is written from the code. A gap found while modelling is tagged `#finding` and links its Issue.
3. **Markdown keeps Mermaid**, generated from the model by `npm run generate` between
   `<!-- likec4-view: <id> -->` markers, so GitHub renders the docs without a build. Generated blocks are never
   edited by hand; `docs/tests/test_architecture_model.py` and `npm run check` fail on drift.
4. GitHub Pages publishes the interactive LikeC4 site under `/architecture-model/` and renders the same views on
   `/docs/architecture/model/`.
5. `likec4` is pinned to an exact version at least two weeks old, in its own npm project (it needs React 19.3;
   Pages pins 19.2).
6. Diagrams outside the architecture model stay Mermaid under ADR-027: domain lifecycles, use cases, per-CU
   sequences, ER diagrams, process flows.

## Options considered

| Option | Verdict |
|--------|---------|
| Keep hand-written Mermaid per document | Rejected: every copy drifts on its own |
| Structurizr DSL | Rejected: needs a Java server or a hosted service for the interactive site |
| LikeC4 with `likec4 gen mermaid` | Rejected as is: drops step numbers, the sequence variant, merged-edge labels and finding styles |
| **LikeC4 with a small Mermaid generator** | **Accepted** |

## Consequences

- One model change updates the SAD, the security, deployment, monitoring and testing docs and Pages.
- The SAD (v4.0) has no PlantUML left; the superseded architecture `.puml` files are archived.
- The Pages artifact grows by about 7 MB (the LikeC4 site).
- A LikeC4 upgrade can change computed views; the generated diff is reviewed in the upgrade PR.
