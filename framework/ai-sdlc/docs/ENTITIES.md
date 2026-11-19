# AI configuration entities

`aisdlc entity` is the factory toolset for skills, agents, rules, MCP servers,
prompts, hooks, and the other catalog kinds. The registry is `REGISTRY` in
`aisdlc/entities.py`. A new kind is a registry entry plus storage under
`[catalogs] dir`; the kit does not import a harness.

| Command | Behavior |
|---|---|
| `list` | Filter by `--kind`, `--scope`, `--harness`, `--version`. `--json` for agents. |
| `visualize` | Mermaid graph of stage links, or `--json` with nodes, edges, and apply drift. |
| `add` | Schema-checked version 1. The same payload again is a no-op. `--dry-run` writes nothing. |
| `edit` | Next immutable revision. `--if-match` is the expected version. |
| `delete` | Refuses when a stage or workflow references the id. `--force` removes it anyway. |
| `apply` | Merges MCP entities into the harness files from `[entities]`. A second run is a no-op. `--dry-run` reports drift. |
| `evolve` | New revision with `--rationale`. `--rollback N` copies that revision forward. |

Factory defaults live in `entities/` (scope `factory`). A project entry with the
same id overrides them. Adopter-specific entities stay in the project's catalog
directory, not in the kit.
