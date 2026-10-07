> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Escritura.numero` (`negocio/Escritura.java`) is a free `int` with no
validation anywhere in the codebase — `EscrituraService#save`
(`service/EscrituraService.java`) persists whatever value is sent. `Folio`
(`negocio/Folio.java`) already carries every piece of scope this control
needs: `fkIdPersonaEscribano` (the acting escribano — `Persona.registroEscribano`
distinguishes escribanos from clients, see `Persona.java`), `anio`, and
`fkIdTipoFolio` (whose `esAuxiliar` flag was introduced by
`protocolo-auxiliar-tramites` to distinguish Protocolo Auxiliar from
Protocolo Principal). `Escritura.folioList` links an escritura to its
folio(s) (from `folio-vinculacion-escritura`, #838, already merged).
`ConstantesNegocio` already defines `ESCRITURA_ANULADA` and
`ESCRITURA_NO_PASO` — the two justified-gap cases CU86 excepción 3.2 names
explicitly. `Escritura.observaciones` already exists and is unused for this
purpose, so it is the natural place to store a gap justification without a
schema change. `protocolo-auxiliar-tramites`'s `ProtocoloAuxiliarService`
already computes `MAX(Escritura.numero) + 1` scoped to auxiliar folios to
auto-assign the number when starting an auxiliar escritura — this change
generalizes that computation into a shared service so both protocols use
one implementation. See proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Validate `Escritura.numero` correlativity scoped to (escribano, año,
  protocolo principal/auxiliar) before saving.
- Reject duplicates within that scope.
- Require and persist a justification for a gap within that scope.
- Keep Protocolo Auxiliar numbering independent of Protocolo Principal.
- Remove the duplicated correlativity computation between this change and
  `protocolo-auxiliar-tramites`.

**Non-Goals:**
- Auto-assigning the número in the Protocolo Principal — CU05 keeps manual
  entry there; only Protocolo Auxiliar auto-assigns (already built by
  `protocolo-auxiliar-tramites`).
- Correcting a número already persisted incorrectly (e.g. from migrated
  data) — out of scope, an administrative concern.
- Any of the other four remaining protocolo areas from Issue #839.

## Decisions

- **New `NumeracionEscrituraService`, shared by `EscrituraService#save` and
  a refactored `ProtocoloAuxiliarService`.** Alternative considered: keep
  the correlativity computation duplicated in each service — rejected,
  violates DRY (`.claude/rules/programming.md`) now that both need the same
  (escribano, año, esAuxiliar)-scoped `MAX(numero)` logic; a shared service
  is the simplest way to keep them consistent as the rule evolves.
- **Scope derived from `Escritura`'s associated `Folio`
  (`fkIdPersonaEscribano`, `anio`, `fkIdTipoFolio.esAuxiliar`), not from new
  fields on `Escritura`.** Alternative considered: duplicate escribano/año/
  esAuxiliar onto `Escritura` itself — rejected, `Folio` is already the
  source of truth for this data (set once, per folio) and duplicating it
  onto `Escritura` risks the two drifting out of sync.
- **Gap justification reuses `Escritura.observaciones`, no new column.**
  Alternative considered: a dedicated `justificacionSalto` field — rejected
  as unnecessary duplication of an existing free-text field that serves the
  same purpose (YAGNI); if a future need arises to distinguish "salto
  justification" from general observations, this can be revisited then.
- **A gap is rejected until justified, not saved with a warning.**
  Alternative considered: save the escritura anyway and only warn —
  rejected because CU86 excepción 3.2 explicitly requires the system to
  "solicitar justificación", and accepting silently defeats the purpose of
  the control; the Escribano must actively confirm the reason before the
  gap is persisted.

## Riesgos / Trade-offs

- [Una escritura sin folio asociado no tiene protocolo/año/escribano
  derivable, dejando indeterminado el alcance de la validación] →
  Mitigation: si la escritura no tiene folio asociado al momento de
  guardar el número, la validación de correlatividad se omite (no hay
  alcance que calcular); esto es consistente con que la vinculación a folio
  (CU87, #838) puede ocurrir en un paso posterior al ingreso del número.
- [Refactorizar `ProtocoloAuxiliarService` para delegar en el nuevo
  servicio puede introducir una regresión en el circuito ya probado de
  `protocolo-auxiliar-tramites` si ese cambio ya está en producción] →
  Mitigation: los tests de `ProtocoloAuxiliarServiceTest` existentes
  (`shouldKeepAuxiliarNumberingIndependentFromPrincipal`,
  `shouldNotGenerateCarpetaForAuxiliarEscritura`) se ejecutan como parte de
  la regresión de este cambio para confirmar que el comportamiento
  observable no cambia (ver Regression Strategy).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Número coincide con el correlativo esperado | unit | `NumeracionEscrituraServiceTest#shouldAcceptNumberMatchingExpectedCorrelativo` |
| Número ya utilizado | integration | `EscrituraControllerTest#shouldRejectDuplicateNumeroWithinScope` |
| Salto sin justificación | integration | `EscrituraControllerTest#shouldRejectGapWithoutJustification` |
| Salto con justificación | integration | `EscrituraControllerTest#shouldAcceptGapWithJustification` |
| Escritura de Protocolo Auxiliar no afecta la numeración del Principal | unit | `NumeracionEscrituraServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` |

- New unit tests (`src/test/java/.../unit/`): `NumeracionEscrituraServiceTest`.
- New/extended integration tests (`src/test/java/.../integration/`):
  `EscrituraControllerTest` (extended).
- Coverage impact: new service fully covered; expected to hold or raise the
  JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `ProtocoloAuxiliarServiceTest` (from
  `protocolo-auxiliar-tramites`) — must keep passing unchanged after
  refactoring it to delegate into `NumeracionEscrituraService`, confirming
  the observable behavior is preserved; `EscrituraControllerTest` — existing
  create/update tests that already use correlative numbers must keep
  passing.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `Escritura` creation in a way this change modifies.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `numeracion-escrituras.spec.ts`.
- Golden path covered: preparar una escritura con el número correlativo
  esperado (se guarda sin pedir nada extra).
- Edge / error paths covered: intentar guardar un número duplicado (mensaje
  de error visible); intentar guardar un número con salto sin justificar
  (formulario pide justificación) y luego con justificación (se guarda).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — no new columns or tables.
- Deployment order / coupling: single deploy; the validation is additive to
  an existing endpoint and does not require a maintenance window.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): preparar una escritura con el
  correlativo esperado (se guarda), intentar un duplicado (se rechaza),
  intentar un salto sin justificación (se rechaza) y con justificación (se
  guarda); `GET /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — no schema changes; reverting the code returns
  `EscrituraService#save` to its previous unvalidated behavior.
- Database rollback: not applicable — no migration to revert.
- Data written under the new behavior after revert: escrituras guardadas
  con número correlativo válido y, en su caso, con justificación en
  `observaciones` — siguen siendo escrituras válidas por el resto del
  sistema tras el revert.
- Blast radius if rollback is delayed: bajo — ningún otro cambio depende de
  `NumeracionEscrituraService`.

## Open Questions

Ninguna.
