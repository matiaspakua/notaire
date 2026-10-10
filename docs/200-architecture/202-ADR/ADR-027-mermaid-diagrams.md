# ADR-027: Mermaid as the Canonical Diagram Language for Active Documentation

**Status:** Accepted  
**Date:** 2026-10-10  
**Deciders:** Owner direction via issue #1415 / CU76  
**Related:** ADR-024, ADR-026, #921, #1197, #1414 (Pages Docs tab)

## Context

Architecture and business documentation historically used PlantUML (116 `.puml` files under
`docs/200-architecture/204-diagrams/`, plus PlantUML fences inside the SAD). A few process docs
already use Mermaid. GitHub Markdown and the GitHub Pages Docs surface render Mermaid natively
(or via a light client library) without a PlantUML CI dependency. Dual languages confuse agents
and produce inconsistent public docs.

## Decision

1. **Mermaid is the sole diagram language for active documentation** (SAD narrative diagrams,
   ADRs, development process docs, MODULE-OWNERSHIP, Pages Docs).
2. **Do not add new PlantUML** (`.puml` or ` ```plantuml ` fences) under active docs paths
   (`docs/100-business/`, `docs/200-architecture/` except migration of legacy sources,
   `docs/300-development/`).
3. **Existing `.puml` sources** remain until migrated; treat them as legacy. Prefer converting
   diagrams when a doc is edited. Bulk conversion is a follow-up, not a blocker.
4. **Archived docs** under `docs/000-archive/` may keep PlantUML without conversion.
5. **Pages** renders Mermaid for ownership and architecture diagrams (#1414).

## Options considered

| Option | Verdict |
|--------|---------|
| Keep PlantUML only | Rejected — poor GitHub/Pages ergonomics, Java renderer cost |
| Dual language forever | Rejected — agent and contributor confusion |
| Mermaid for active docs | **Accepted** |

## Consequences

- New diagrams ship as Mermaid fenced blocks or `.mmd` companions.
- SAD primary views used on Pages are rewritten or summarized in Mermaid when touched.
- `docs/200-architecture/204-diagrams/README.md` carries the policy for contributors.
- Agents and OpenSpec design docs use Mermaid exclusively going forward.

## Navigation

- [Diagrams README](../204-diagrams/README.md)
- [MODULE-OWNERSHIP.md](../../300-development/MODULE-OWNERSHIP.md)
- [ADR index](README.md)
