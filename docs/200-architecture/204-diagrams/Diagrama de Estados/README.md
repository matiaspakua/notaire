# Diagramas de estados (legado)

Los `.puml` de esta carpeta no son la fuente de verdad ([ADR-027](../../202-ADR/ADR-027-mermaid-diagrams.md)).

El modelo vigente — gestión, escritura, testimonio, minuta, documentación,
presupuesto, pago y carpeta — está en
[CICLO-DE-VIDA.md](../../203-design/CICLO-DE-VIDA.md).

`transicion-de-estados.puml` es la actividad de negocio del mismo grafo: el
archivo sale de Documentación completa y el testimonio retirado es final.
`workflow-gestion.puml` es el alta por `POST /api/v1/managements/complete-case`.
El paso a paso con dependencias está en
[GESTION.md](../../203-design/GESTION.md).
