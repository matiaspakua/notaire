> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`TipoDeFolio` (`negocio/TipoDeFolio.java`) only models `nombre`, `observaciones`
and `habilitado` — nothing distinguishes a "Protocolo Auxiliar" folio type
from a "Protocolo Principal" one today. `Folio.fkIdEscritura` already exists
(from `folio-vinculacion-escritura`, #838), so "folio disponible" can already
be expressed as "folio without an associated `Escritura`". `Escritura.numero`
is a free `int`, set directly by the caller with no correlativity
enforcement anywhere in the codebase — this change does not add validation to
that field (that is `protocolo-numeracion-escrituras`'s job); it only
computes the *suggested next value scoped to auxiliar folios* when creating
the escritura. `Tramite.fkIdGestion` (`negocio/Tramite.java`) is a nullable
`@ManyToOne` (not `optional = false`), so a `Tramite` can already exist
without a `GestionDeEscritura` — this is the concrete mechanism that makes
"no carpeta" a natural consequence rather than a special case: `carpetas-de-tramite`
(`protocolo-carpetas-de-tramite`) only auto-generates a `CarpetaTramite` for a
trámite that belongs to a gestión. A `TipoDeFolioController` and an admin
screen (`frontend/src/app/dashboard/administracion/folios/page.tsx`) already
exist for `TipoDeFolio`. See proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Let a `TipoDeFolio` be flagged as belonging to Protocolo Auxiliar.
- List available auxiliar folios (no `Escritura` associated).
- Start an `Escritura` on an available auxiliar folio with a correlative
  number scoped to the auxiliar protocol, independent of the principal
  protocol's numbering.
- Confirm, by construction, that no `CarpetaTramite` is generated for this
  circuit.

**Non-Goals:**
- Signing, testimonio generation, or delivery — those are covered by
  `escritura-post-firma-legal-cycle` (#832) and reused unmodified.
- General correlativity *validation* (detecting gaps/duplicates) across
  either protocol — that is `protocolo-numeracion-escrituras` (CU86), which
  reuses the `esAuxiliar` flag introduced here.
- Any of the other three remaining protocolo areas from Issue #839.

## Decisions

- **`esAuxiliar: boolean` on `TipoDeFolio`, default `false`.** Alternative
  considered: a new `TipoProtocolo` enum/entity (`PRINCIPAL` / `AUXILIAR`) —
  rejected as over-engineering for a binary distinction that only this
  change and `protocolo-numeracion-escrituras` consume; a boolean is the
  simplest model that satisfies both. If a third protocolo type ever
  appears, this can be revisited then (YAGNI).
- **Auxiliar numbering computed as `MAX(Escritura.numero) + 1` scoped to
  escrituras on folios where `TipoDeFolio.esAuxiliar = true`, not a separate
  sequence/counter table.** Alternative considered: a dedicated
  `numeracion_auxiliar` counter — rejected because `Escritura.numero` already
  exists and is queryable via the existing `Folio → TipoDeFolio` join;
  introducing a counter table would duplicate state that can drift out of
  sync with the actual data.
- **No special-case in `carpetas-de-tramite` for "protocolo auxiliar".**
  Alternative considered: add an explicit `esCircuitoAuxiliar` flag to
  `Tramite` and check it in `CarpetaTramiteService` — rejected because
  `Tramite.fkIdGestion` being nullable already encodes "no gestión ⇒ no
  carpeta" without any new field; the auxiliar `Tramite` created by this
  change simply leaves `fkIdGestion` unset.
- **Reuse the existing `TipoDeFolio` admin screen for the new checkbox.**
  Alternative considered: a dedicated "tipos de folio auxiliar" screen —
  rejected, unnecessary duplication of an existing CRUD screen for one new
  field.

## Riesgos / Trade-offs

- [`esAuxiliar` es un campo libre editable en cualquier momento; marcar/desmarcar
  un tipo de folio que ya tiene folios con escrituras asociadas podría alterar
  retroactivamente qué cuenta como "numeración auxiliar"] → Mitigation:
  fuera de alcance de este cambio bloquear la edición de `esAuxiliar` una vez
  que el tipo tiene folios en uso; se documenta como riesgo operativo — el
  Escribano es responsable de no reclasificar tipos de folio en uso. Si esto
  resulta insuficiente en producción, es una extensión aditiva (agregar una
  validación de "tipo con folios en uso no editable").
- [La correlatividad auxiliar calculada por `MAX+1` sobre escrituras
  existentes es más lenta que un contador dedicado a medida que crece el
  volumen] → Mitigation: el volumen esperado de escrituras de Protocolo
  Auxiliar es bajo comparado al Principal (CU81 es para trámites simples);
  se acepta el costo de la consulta por ahora, ver Non-Goals.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Marcar un tipo de folio como auxiliar | integration | `TipoDeFolioControllerTest#shouldMarkTipoDeFolioAsAuxiliar` |
| Tipo de folio sin marcar es de Protocolo Principal | unit | `TipoDeFolioTest#shouldDefaultEsAuxiliarToFalse` |
| Hay folios auxiliares disponibles | integration | `ProtocoloAuxiliarControllerTest#shouldListAvailableFoliosAuxiliares` |
| No hay folios auxiliares disponibles | integration | `ProtocoloAuxiliarControllerTest#shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable` |
| Alta de escritura de Protocolo Auxiliar con folio disponible | integration | `ProtocoloAuxiliarControllerTest#shouldCreateEscrituraOnAvailableFolioAuxiliar` |
| Intento de iniciar escritura sin folio auxiliar disponible | integration | `ProtocoloAuxiliarControllerTest#shouldRejectEscrituraWhenNoFolioAuxiliarAvailable` |
| Numeración del Protocolo Auxiliar independiente del Principal | unit | `ProtocoloAuxiliarServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` |
| Escritura de Protocolo Auxiliar sin carpeta | unit | `ProtocoloAuxiliarServiceTest#shouldNotGenerateCarpetaForAuxiliarEscritura` |

- New unit tests (`src/test/java/.../unit/`): `TipoDeFolioTest`, `ProtocoloAuxiliarServiceTest`.
- New integration tests (`src/test/java/.../integration/`): `TipoDeFolioControllerTest` (extended), `ProtocoloAuxiliarControllerTest`.
- Coverage impact: new field + service + controller fully covered; expected to hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `TipoDeFolioControllerTest` — existing assertions
  unaffected; `esAuxiliar` is a new field defaulting to `false`.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `TipoDeFolio` or `Escritura` creation in a way this change modifies.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `protocolo-auxiliar.spec.ts`.
- Golden path covered: marcar un tipo de folio como auxiliar (pantalla de
  administración de folios), listar folios auxiliares disponibles, iniciar
  una escritura de Protocolo Auxiliar.
- Edge / error paths covered: intentar iniciar una escritura sin folios
  auxiliares disponibles (mensaje de error visible).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes — `V{n}__add_es_auxiliar_to_tipos_folio.sql`
  (columna `es_auxiliar boolean NOT NULL DEFAULT false` en `tipos_folio`).
- Deployment order / coupling: migración y código se despliegan juntos (un
  solo deploy); la migración es aditiva, no requiere ventana de
  mantenimiento.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): marcar un tipo de folio como auxiliar,
  confirmar que aparece en el listado de folios disponibles, iniciar una
  escritura de Protocolo Auxiliar y confirmar que no se genera carpeta de
  trámite; `GET /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — código aditivo; revertir el código deja la columna
  `es_auxiliar` sin uso pero sin romper ninguna funcionalidad existente.
- Database rollback: `R{n}` opcional para eliminar la columna `es_auxiliar`
  si se decide no continuar; no es obligatorio revertir el esquema para
  revertir el código.
- Data written under the new behavior after revert: los tipos de folio
  marcados como auxiliares y las escrituras ya creadas quedan en la base sin
  ser accesibles vía el nuevo endpoint hasta un nuevo deploy; siguen siendo
  escrituras válidas por el resto del sistema.
- Blast radius if rollback is delayed: bajo — `protocolo-numeracion-escrituras`
  reutiliza el campo `esAuxiliar`, así que si ese cambio ya está desplegado,
  revertir este introduce inconsistencia; coordinar el rollback con ese
  change si ambos están en producción.

## Open Questions

Ninguna.
