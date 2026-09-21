# Explore: Issue #973 — Translate domain model to English

Source: https://github.com/matiaspakua/notaire/issues/973

## What the epic assumed vs. what's actually true today

The epic text describes the domain as "currently modeled in Spanish across
the entire stack: JPA entities, DTOs, repositories, controllers, REST
contracts, the PostgreSQL schema, frontend types, and tests." A codebase
sweep shows that assumption is now stale — most of it already happened,
apparently as incremental work over time, without the epic being updated:

| Layer | State found | Evidence |
|---|---|---|
| JPA entity/DTO/repository/controller class names | **Already English** | `business/` has `Deed`, `Copy`, `Budget`, `Concept`, `Testimony`, `Procedure`, `Substitution`, `Person`, etc. No `Escritura.java`, `Presupuesto.java`, `Tramite.java`, `Suplencia.java`, `Concepto.java`, `Testimonio.java`, `Copia.java` remain. |
| Entity field names | **Already English** | e.g. `Deed.dateDeedrecording`, `Deed.number` |
| REST JSON contract fields | **Already English** (spot-checked) | No Spanish field names found live in frontend `src/` for the fields checked |
| Frontend types/components | **Already English** (spot-checked) | No hits for `numero_identificacion`, `fecha_escrituracion`, `nombre_tramite` |
| `people` table (Persona slice) | **Done** | `V25__rename_personas_to_people.sql`, merged, OpenSpec change `rename-persona-to-person` archived |
| Remaining DB tables/columns | **Still Spanish** | `@Table(name = "escrituras")`, `"presupuestos"`, `"conceptos"`, `"tramites"`, `"suplencias"`, `"testimonios"`, `"copias"`, `"gestiones_de_escrituras"`, `"historial"`, `"pagos"`, `"items"`, `"inmuebles"`, `"documentos_presentados"`, `"tipos_de_*"`, `"plantilla_*"`, `"movimientos_testimonio"`, `"folios_copias"`, `"tramites_personas"`, `"cuadernos"`, `"carpetas_tramite"`, `"minutas_inscripcion"`, `"identificaciones"`, `"registro_auditoria"` |
| Named JPQL queries | **Still Spanish-named** | e.g. `@NamedQuery(name = "Escritura.findByFechaEscrituracion", ...)` — the query *name* string, not the JPQL body (which already reads `SELECT e FROM Deed e`) |

**Conclusion: the epic's real remaining scope is a single layer — the
PostgreSQL schema (table + column + sequence names) — not a full-stack
rename.** The Java/DTO/API/frontend rename this epic was written to drive
already happened (git log shows `8eea04c refactor(person): rename Persona
domain to Person (first vertical slice)` and `696b38f refactor(backend):
mass-rename domain model, controllers, services...` — the "mass-rename"
commit suggests the Java-layer work was done in one broad pass rather than
per-entity slices, ahead of/alongside the epic's stated vertical-slice
plan). `@NamedQuery` name strings are cosmetic (JPA never uses them as SQL
identifiers) and low priority — worth a pass but not blocking.

## Why this matters for how to close the epic

The epic's acceptance criteria ("entity/DTO/repository/controller names",
"REST contracts", "frontend types") are effectively already met. Only one
unchecked box has real work behind it:

```
- [x] All backend entity/DTO/repository/controller/field names translated to English
- [ ] All DB tables/columns renamed via new Flyway migrations (old migrations untouched)
- [x] REST contracts (JSON field names, URL paths) translated to English
- [x] Frontend types/components/i18n keys translated to English
- [ ] All backend + E2E tests renamed/translated accordingly, full suite green   <- depends on DB rename only if test SQL/fixtures reference raw table/column names
- [x] docs/ folder left untouched (Spanish)
- [x] Each slice merged independently (people slice already merged this way)
```

Reframing this as a **schema-only rename epic** changes the sequencing
strategy from "one slice per business entity, touching every layer" to "one
Flyway migration (+ `@Table`/`@Column` annotation update) per table
cluster, no Java class/field renames needed." That collapses ~10 previously
"full-stack" slices into much smaller, lower-risk, single-layer PRs.

## Sequencing options for the DB-only rename

Ordered by fan-out (FK in-degree/out-degree), matching the epic's own
"start with leaf entities" instruction — now applied to *tables* rather
than entities:

```
Leaf / low fan-out (do first, validates the pattern again post-people):
  identificaciones, tipos_identificacion, tipos_de_documento,
  tipos_de_folio, tipos_de_tramite, roles, estados_de_gestion,
  registro_auditoria, cuadernos

Mid fan-out (referenced by 1-2 other tables):
  inmuebles, conceptos, items, pagos, copias, folios_copias,
  movimientos_testimonio, documentos_presentados,
  plantilla_tramites, plantilla_presupuestos, plantilla_costos_documento,
  carpetas_tramite, minutas_inscripcion

High fan-out / central tables (do last, highest blast radius):
  tramites, escrituras, gestiones_de_escrituras, presupuestos,
  testimonios, suplencias, historial, tramites_personas, folios (partly done)
```

Each slice is still: one new `V{n}__rename_x_to_y.sql` (never edit
existing migrations, per Constitution P6) + update the corresponding
`@Table`/`@Column` annotations in the already-English-named Java entity +
run `mvn test -Ppg-integration` (`FlywaySchemaValidationIntegrationTest`) +
regression. No frontend, DTO, or controller changes expected unless a test
or Bruno fixture hardcodes a raw SQL column name.

## Risks / unknowns

- **`@NamedQuery` name strings** (e.g. `"Escritura.findByFechaEscrituracion"`)
  are unrelated to the DB rename and cosmetic; worth a cheap cleanup pass
  but shouldn't gate the schema slices.
- **Test/Bruno fixtures with raw SQL** — the "full suite green" criterion
  in the epic assumes tests need renaming; if none reference raw
  table/column names directly, that box may already be satisfiable per
  slice with zero test changes. Needs a grep pass per slice, not upfront.
- **FK columns on other tables pointing at a renamed table** — the `people`
  slice's own precedent (V25 migration comment) explicitly left FK columns
  like `fk_id_persona_cliente` on other tables untouched, deferring them to
  each *owning* table's own slice. The same convention should carry
  forward, or FK column renames will need their own pass after all target
  tables are renamed.
- **`historial`, `registro_auditoria`** carry compliance/audit semantics
  (`AuditoriaAspect`) — verify nothing outside JPA (e.g. raw SQL in
  `AuditoriaAspect`, Grafana/Loki queries, `docs/`) hardcodes these Spanish
  table names before renaming.
- Given how much smaller the real remaining scope is, it's worth asking
  whether this still warrants an "epic" with N sub-issues, or a single
  batched issue with N small sequential PRs under one OpenSpec change
  (this one) — leaning toward the latter given the pattern is now proven
  twice over (people slice, and the mass Java rename).

## Recommended next step

Treat issue #973 as **scoped down** rather than closed outright: update
its description/acceptance criteria to reflect that the entity/DTO/API/
frontend layers are done, and retarget it purely at the DB schema rename
(with `@NamedQuery` name cleanup as an optional add-on). This OpenSpec
change (`translate-domain-schema-to-english`) captures that narrowed scope;
`proposal.md` should reference #973 directly rather than opening a new
sub-issue, since the "vertical slice per entity" sub-issue model no longer
matches the (now single-layer) remaining work.
