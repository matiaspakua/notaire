# Evaluación de la documentación de negocio y de diseño

Corte de octubre 2026, sobre el árbol `docs/100-business` y
`docs/200-architecture`, contrastado con Flyway y los adaptadores HTTP.
El inventario estructural anterior está en
[DOCUMENTATION-AUDIT-2026-10.md](../300-development/DOCUMENTATION-AUDIT-2026-10.md)
(issue #921). Este corte pregunta otra cosa: si el relato del trámite, que es
el proceso central, está completo y coincide con el sistema.

## 1. Requisitos

No falta un número de caso de uso. CU01 a CU87 están, sin hueco. El catálogo
de requisitos sigue en `101-requirements/requerimientos.csv`. La matriz
RF ↔ CU sigue en `104-traceability/`.

Lo que no alcanza es tratar el SRS como especificación viva. `SRS -
Especificacion de Requerimientos.md` describe MySQL y un escritorio. Es la
línea de base histórica del relevamiento. La especificación vigente es el CSV,
las fichas de caso de uso y el ciclo de vida.

Tampoco falta un requisito nuevo para «casos». Faltaba el esquema de análisis
que une los casos que ya existen en un solo recorrido con sus dependencias.
Ese esquema es [GESTION.md](../200-architecture/203-design/GESTION.md).

## 2. El diagrama de actividad

`204-diagrams/Diagrama de Estados/transicion-de-estados.puml` se leía como la
transición de estados de una gestión. No lo era, y no coincidía con el grafo
sembrado (`V10`, `V41`):

| Lo que decía la actividad | Lo que hace el sistema |
|---------------------------|------------------------|
| Archiva al final, después de retirar testimonios y mirar la deuda | La arista a `Archivada` sale solo de `Documentacion Completa`. `Testimonio Retirado` ya es final |
| Anular y «no pasó» son ramas de la gestión | Son valores de `deeds.status`. No hay arista sembrada |
| Repite el ciclo por cada trámite de la gestión | El nodo es uno por gestión, no un bucle por trámite |
| El Registro observado vuelve a ingresarse dentro de la gestión | La minuta `Observada` no tiene vuelta en el servicio. El reingreso de testimonio es otra máquina |

La actividad se reescribió para contar el mismo recorrido que el grafo y que
[GESTION.md](../200-architecture/203-design/GESTION.md). Sigue siendo legado
([ADR-027](../200-architecture/202-ADR/ADR-027-mermaid-diagrams.md)). La fuente
de los estados es [CICLO-DE-VIDA.md](../200-architecture/203-design/CICLO-DE-VIDA.md) §3.1.

`workflow-gestion.puml` mostraba `POST /api/v1/gestion` y la tabla
`gestiones_de_escrituras`. El alta del caso es
`POST /api/v1/managements/complete-case`. La tabla se llama `deed_managements`
desde `V32`. El archivo se corrigió. Sigue siendo legado.

## 3. Esquema de análisis y diseño por proceso mayor

| Proceso | Casos | Estados | Secuencias del comportamiento actual | Hueco que queda |
|---------|-------|---------|--------------------------------------|-----------------|
| Gestión | CU02, CU14, CU16, CU83, CU85 | CICLO §3.1 | [GESTION.md](../200-architecture/203-design/GESTION.md) §3 | Ninguno en el camino sembrado |
| Escritura | CU05, CU06, CU52 | CICLO §3.3 | GESTION §3.3 | Anulada, No Paso e Inscripta no tienen servicio propio |
| Testimonio | CU07, CU11, CU12, CU44 | CICLO §3.4 | GESTION §3.4 | El reingreso está en el servicio y en CICLO, no en una secuencia propia |
| Minuta | CU82 | CICLO §3.5 | GESTION §3.6 | Sin vuelta desde Observada |
| Documentación | CU04, CU10, CU43, CU72 | CICLO §3.6 | CICLO §4.2 y GESTION §3.2 | — |
| Presupuesto y pago | CU01, CU15, CU45, CU47 | CICLO §3.7 y §3.8 | CICLO §4.3 | El orden del presupuesto no está custodiado por aristas |
| Carpeta | CU85 | CICLO §3.9 | GESTION §3.5 | — |

Los `.puml` de `204-diagrams/Secuencias/` cubren casi todos los casos de uso
viejos. No se revalidaron uno por uno en este corte: ADR-027 los deja como
legado y el comportamiento vigente de los procesos mayores está en las
secuencias Mermaid de arriba. Un cambio de un caso de uso actualiza la ficha
y, si mueve un estado o una llamada de estos procesos, la fila de esta tabla.

## 4. Coherencia

- El SAD §6 apunta al ciclo de vida y, desde esta evaluación, al esquema de la
  gestión. No dibuja una segunda máquina.
- ADR-014 sigue siendo la decisión del motor. No hace falta otro ADR para
  repetir el grafo.
- La regla de la flota: cada implementación valida los documentos que su
  propia trazabilidad nombra. Está en la Constitución §8 y la ejecuta
  `workspace/sdlc/check-doc-traceability.py`.

## 5. Qué no se hizo

No se reescribió cada ficha CU01–CU87. El relato paso a paso del actor sigue
en la ficha. Copiar el grafo en cada una vuelve a separar las fuentes.

No se cerró el issue #921. El diccionario de datos (hallazgo F3 de la auditoría
de octubre) y la licencia siguen abiertos ahí.
