# Architecture diagrams

## Policy (ADR-033, ADR-027)

| Diagram | Source | Rule |
|---------|--------|------|
| Architecture: context, containers, hexagons, security, testing and delivery, deployment | [LikeC4 model](../210-architecture-model/README.md) | Edit the `.c4` files, run `npm run generate`; never edit a block between `likec4-view` markers |
| Domain lifecycles, use cases, per-CU sequences, ER, process flows | Mermaid fences in the consuming doc | ADR-027 |
| New PlantUML | — | Not allowed in active docs |

Legacy PlantUML sources in this directory (`data-model-core.puml`, `Casos de Uso/`, `Secuencias/`,
`Diagrama de Clases/`, `Diagrama de Estados/`) stay until migrated. They are **not** the source of truth for new
work. The architecture PlantUML files that the model replaced (`architecture-legacy`, `architecture-target`,
`deployment-docker`, `backend-package-structure`, `migration-phases`) are archived in
[`docs/000-archive/200-architecture/204-diagrams/`](../../000-archive/200-architecture/204-diagrams/).

## Canonical views

| View | Location |
|------|----------|
| Architecture model (14+ views, interactive) | [`210-architecture-model/`](../210-architecture-model/README.md), Pages `/docs/architecture/model/` and `/architecture-model/` |
| Domain lifecycles (states, sequences, components) | [`CICLO-DE-VIDA.md`](../203-design/CICLO-DE-VIDA.md) |
| Module ownership / dependencies | [`docs/300-development/MODULE-OWNERSHIP.md`](../../300-development/MODULE-OWNERSHIP.md) |

## Related

- [ADR-033](../202-ADR/ADR-033-likec4-architecture-model.md)
- [ADR-027](../202-ADR/ADR-027-mermaid-diagrams.md)
- [SAD](../201-SAD/sad.md)
