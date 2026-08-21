> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`GestionDeEscritura` (`negocio/GestionDeEscritura.java`) has a `numero` field
whose Javadoc informally calls it "número de carpeta", but there is no
`CarpetaTramite` entity, no per-trámite carpeta, and no activa/espera/archivada
state anywhere in the codebase. `Tramite` (`negocio/Tramite.java`) already has
a `@ManyToOne fkIdGestion`, so a gestión with several trámites is already
modeled — CU85's "una carpeta por trámite" exception maps directly onto that
existing relationship. Archiving a gestión already has a real hook point:
`GestionArchiveDebtService.archivar(Integer idGestion)`
(`service/GestionArchiveDebtService.java`), which sets
`EstadoDeGestion = "Archivada"` and is invoked from `POST
/api/v1/gestiones/{id}/archivar` in `GestionController`. See proposal.md —
Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Auto-generate one `CarpetaTramite` per `Tramite` when a trámite starts.
- Model and expose the activa/espera/archivada lifecycle of a carpeta.
- Cascade a gestión's archiving into its carpetas' archiving, with the
  espera-without-motivo-resuelto guard from CU85's excepción 5.1.

**Non-Goals:**
- Any of the other four protocolo areas from Issue #839.
- Physical or digital document management inside the carpeta — that already
  exists via `DocumentoPresentado` and is untouched by this change.
- Renaming or repurposing `GestionDeEscritura.numero` — it stays as-is;
  `CarpetaTramite.numero` is a new, separate numbering.

## Decisions

- **`CarpetaTramite` keyed on `Tramite`, not on `GestionDeEscritura`.**
  Alternative considered: one carpeta per gestión (matching the informal
  "número de carpeta" reading of `GestionDeEscritura.numero`) — rejected
  because CU85's excepción 2.1 is explicit: a gestión with several trámites
  gets one carpeta *per trámite*, so `Tramite` is the correct owning side.
- **Auto-generation triggered from trámite creation, not a separate manual
  step.** Alternative considered: a dedicated "crear carpeta" action the user
  triggers after creating the trámite — rejected because CU85's paso 2 says
  the system generates the carpeta automatically; a manual step would allow a
  trámite to exist without its carpeta, contradicting the Use Case.
  Concretely: `CarpetaTramiteService` exposes a method invoked at the point
  where a `Tramite` is persisted, mirroring how `GestionArchiveDebtService` is
  invoked at the point where a gestión is archived — no new generic "hook"
  mechanism is introduced.
- **Archiving cascade lives in `GestionArchiveDebtService.archivar`, not in a
  new orchestrator.** Alternative considered: a separate
  `CarpetaTramiteArchiveService` called independently by the controller —
  rejected because that would let a caller archive the gestión without
  archiving its carpetas (or vice versa) by simply not calling the second
  service; putting the cascade inside the existing, single archiving
  entrypoint keeps the invariant enforced in one place.
- **Espera-without-motivo-resuelto guard modeled as "still in Espera",
  not a separate "resuelto" flag.** A carpeta's motivo is "resuelto" simply
  by the carpeta leaving Espera (back to Activa, or forward to Archivada via
  explicit confirmation) — no new status field. Alternative considered: add a
  `motivoResuelto: boolean` — rejected as redundant, since state transition
  already encodes it and CU85 does not require tracking resolution history.

## Riesgos / Trade-offs

- [Archivar una gestión con muchas carpetas en espera obliga a una
  confirmación explícita por endpoint, no por carpeta individual] →
  Mitigation: el endpoint de archivado devuelve la lista de carpetas en
  espera sin resolver junto con el aviso; la confirmación es a nivel gestión
  (como ya lo es hoy `deudaPendienteAlArchivar`), consistente con el patrón
  existente de `GestionArchiveDebtService`.
- [Numeración de `CarpetaTramite.numero` es un concepto nuevo, distinto y sin
  relación con `GestionDeEscritura.numero`, lo que puede confundir en el
  futuro] → Mitigation: documentado explícitamente en proposal.md — Out of
  Scope, y el Javadoc de `GestionDeEscritura` no se modifica ni se referencia
  desde la nueva entidad.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Alta de un trámite único en la gestión | unit | `CarpetaTramiteServiceTest#shouldGenerateCarpetaOnSingleTramiteGestion` |
| Gestión que agrupa más de un trámite | unit | `CarpetaTramiteServiceTest#shouldGenerateOneCarpetaPerTramiteInMultiTramiteGestion` |
| Consulta de una carpeta existente | integration | `CarpetaTramiteControllerTest#shouldReturnCarpetaByTramite` |
| Consulta de una carpeta inexistente | integration | `CarpetaTramiteControllerTest#shouldReturnNotFoundForMissingCarpeta` |
| Carpeta puesta en espera con motivo | integration | `CarpetaTramiteControllerTest#shouldSetCarpetaToEsperaWithMotivo` |
| Intento de poner en espera sin motivo | integration | `CarpetaTramiteControllerTest#shouldRejectEsperaWithoutMotivo` |
| Archivado de gestión con todas las carpetas activas | unit | `GestionArchiveDebtServiceTest#shouldArchiveAllActiveCarpetasOnGestionArchive` |
| Archivado de gestión con una carpeta en espera sin resolver | unit | `GestionArchiveDebtServiceTest#shouldRequireConfirmationWhenCarpetaInEsperaUnresolved` |
| Confirmación explícita de archivado con carpeta en espera | unit | `GestionArchiveDebtServiceTest#shouldArchiveCarpetaInEsperaOnExplicitConfirmation` |

- New unit tests (`src/test/java/.../unit/`): `CarpetaTramiteServiceTest` (auto-generation), extend `GestionArchiveDebtServiceTest` (cascade + confirmation guard).
- New integration tests (`src/test/java/.../integration/`): `CarpetaTramiteControllerTest`.
- Coverage impact: new entity/service/controller fully covered; expected to hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `GestionArchiveDebtServiceTest` — existing
  assertions about `deudaPendienteAlArchivar` and `EstadoDeGestion` remain
  unchanged; the new cascade is additive behavior on the same method.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `GestionDeEscritura` archiving or `Tramite` creation in a way this
  change modifies.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `carpetas-de-tramite.spec.ts`.
- Golden path covered: iniciar un trámite, ver su carpeta generada en estado
  "Activa", ponerla en espera con motivo, y verificar que se archiva al
  archivar la gestión.
- Edge / error paths covered: intentar poner en espera sin motivo (mensaje de
  error visible); intentar archivar una gestión con una carpeta en espera sin
  resolver (aviso visible y confirmación explícita requerida).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes — `V{n}__create_carpetas_tramite_table.sql`
  (nueva tabla `carpetas_tramite`).
- Deployment order / coupling: migración y código se despliegan juntos (un
  solo deploy); la migración es aditiva, no requiere ventana de
  mantenimiento.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): iniciar un trámite de prueba vía UI,
  confirmar que se genera su carpeta en estado "Activa"; archivar la gestión
  y confirmar que la carpeta pasa a "Archivada"; `GET /actuator/health` en
  verde.

## Rollback Strategy

- Revert safe: yes — código aditivo; revertir el código deja la tabla
  `carpetas_tramite` sin uso pero sin romper ninguna funcionalidad existente,
  incluyendo el archivado de gestiones (que vuelve a su comportamiento
  actual).
- Database rollback: `R{n}` opcional para eliminar `carpetas_tramite` si se
  decide no continuar; no es obligatorio revertir el esquema para revertir el
  código.
- Data written under the new behavior after revert: las carpetas ya
  generadas quedan en la base sin ser accesibles vía API hasta un nuevo
  deploy.
- Blast radius if rollback is delayed: ninguno — ninguna otra capability
  depende de `CarpetaTramite`.

## Open Questions

Ninguna.
