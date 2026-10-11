# Documentation — Notaire

Navigation index for all Notaire project documentation, organized by lifecycle phase:
business, architecture, and development.

## Structure

| Folder | Contents |
|---------|-----------|
| [`100-business/`](100-business/) | Requirements, use cases, actors, and business traceability |
| [`200-architecture/`](200-architecture/) | SAD (arc42), ADRs, design, diagrams, data model, security, monitoring, DevSecOps, and deployment |
| [`300-development/`](300-development/) | Setup guides, code standards, testing, and the [AI SDLC Cloud fleet](300-development/304-ai-sdlc-cloud/); current audit: [DOCUMENTATION-AUDIT-2026-10.md](300-development/DOCUMENTATION-AUDIT-2026-10.md) |
| [`github/`](github/) | GitHub organization (board, milestones, labels) and the September 2026 production-readiness audit |
| [`000-archive/`](000-archive/) | Superseded legacy documents and artifacts, kept for historical traceability |

## Recommended entry point

1. **Business**: [Use cases](100-business/102-use-cases/) — what the system does and for whom.
2. **Lifecycle**: [CICLO-DE-VIDA.md](200-architecture/203-design/CICLO-DE-VIDA.md) — states, sequences, and C4 views of the product.
3. **Method**: [INGENIERIA.md](300-development/INGENIERIA.md) — artifacts, traceability, and processes.
4. **Architecture**: [SAD](200-architecture/201-SAD/sad.md) — the full arc42 view of the system.
5. **Development**: [`CLAUDE.md`](../CLAUDE.md) and [`CONSTITUTION.md`](../CONSTITUTION.md) — the mandatory workflow for any code change.
6. **Delivery**: [GitHub organization](github/README.md) — where the work is tracked and how it is labeled.

## Conventions

- Every numbered folder (`1XX-`, `2XX-`, `3XX-`) follows the suggested reading order inside its phase.
- Active diagrams are Mermaid inside Markdown ([ADR-027](200-architecture/202-ADR/ADR-027-mermaid-diagrams.md)). Remaining `.puml` files are legacy.
- Superseded documents (legacy formats such as `.drawio`/`.jpg`, or replaced content) move to `000-archive/` with a `README.md` that links to the current equivalent, and a redirect note stays in the original folder.
