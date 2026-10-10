# Architecture diagrams

## Policy (ADR-027)

**Mermaid is the only diagram language for active documentation.**

| Do | Do not |
|----|--------|
| Add Mermaid fences (` ```mermaid `) in Markdown | Add new `.puml` files for active docs |
| Put reusable Mermaid sources as `.mmd` next to the doc when helpful | Add new ` ```plantuml ` fences in SAD / ADRs / process docs |
| Migrate a PlantUML diagram when you edit its consuming doc | Leave dual contradictory diagrams for the same view |

Legacy PlantUML sources in this directory (`.puml`, including `Casos de Uso/`, `Secuencias/`,
`Diagrama de Clases/`, `Diagrama de Estados/`) stay until migrated. They are **not** the source of
truth for new work. Archived copies may also live under `docs/000-archive/`.

## Canonical Mermaid views

| View | Location |
|------|----------|
| Module ownership / dependencies | [`docs/300-development/MODULE-OWNERSHIP.md`](../../300-development/MODULE-OWNERSHIP.md) |
| Pages Docs (rendered) | `github-page/app/docs/` |

## Related

- [ADR-027](../202-ADR/ADR-027-mermaid-diagrams.md)
- [SAD](../201-SAD/sad.md)
