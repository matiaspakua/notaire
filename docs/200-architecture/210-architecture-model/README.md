# Architecture model (LikeC4)

The single source of the Notaire architecture views ([ADR-033](../202-ADR/ADR-033-likec4-architecture-model.md),
#2483). The model is written from the code, not from older diagrams; each gap found while writing it is tagged
`#finding` and links its GitHub Issue.

| File | Holds |
|------|-------|
| `src/spec.c4` | Element, deployment-node and relationship kinds, tags |
| `src/system.c4` | People, the Notaire system and its containers, observability, GitHub |
| `src/backend.c4` / `src/frontend.c4` | The two hexagons (ADR-030) |
| `src/security.c4` | Security controls and data protection (CU78) |
| `src/delivery.c4` | Test pyramid, quality gates, security scanning, workflows (CU76) |
| `src/deployment.c4` | Development, production and staging (Kustomize) topology |
| `src/views.c4` | The 14 views; their ids are stable |
| `scripts/mermaid.mjs` | Computed view to Mermaid (pure, unit-tested) |
| `scripts/generate.mjs` | Writes every generated copy; `--check` fails on drift |
| `generated/mermaid/*.mmd` | Generated, committed. Do not edit |

## Commands

```bash
cd docs/200-architecture/210-architecture-model
npm ci
npm run validate          # syntax and semantics
npm run dev               # live preview at http://localhost:5173
npm run generate          # .mmd files, github-page/lib/architecture-model.json, doc blocks
npm run check             # exit 1 when a generated copy drifted
node --test scripts/      # generator unit tests
```

`python3 docs/tests/test_architecture_model.py` (in `docs/verify.sh`) checks the same copies without Node.

## Embedding a view in Markdown

Write the markers with an empty fence and run `npm run generate`; the generator fills and keeps the block:

````markdown
<!-- likec4-view: containers -->
```mermaid
```
<!-- /likec4-view -->
````

## Published

GitHub Pages builds the interactive site into `/architecture-model/` (hash routing, so a view is
`/notaire/architecture-model/#/view/<id>/`) and renders every view on `/docs/architecture/model/`.

## Upgrading LikeC4

Pin an exact version that is at least two weeks old, run `npm run generate` and review the diff of
`generated/`: a new release can change how views are computed.
