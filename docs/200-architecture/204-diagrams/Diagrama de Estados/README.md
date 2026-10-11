# State diagrams (legacy)

The `.puml` files in this folder are not the source of truth
([ADR-027](../../202-ADR/ADR-027-mermaid-diagrams.md)).

The current model — management, deed, testimony, registration draft,
documents, budget, payment, and procedure folder — is in
[CICLO-DE-VIDA.md](../../203-design/CICLO-DE-VIDA.md).

`transicion-de-estados.puml` is the business activity of that same graph:
archive leaves Documentación completa, and a withdrawn testimony is final.
`workflow-gestion.puml` is the create call
`POST /api/v1/managements/complete-case`. The step-by-step dependencies are in
[GESTION.md](../../203-design/GESTION.md).
