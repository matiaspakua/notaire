# Controlar la numeración correlativa de escrituras

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #839 |
| Use Case | CU86 – Controlar Numeración Correlativa de Escrituras |
| Branch | `feat/839_protocolo-numeracion-escrituras` |
| Gate 1 status | pending |

## Objetivo

`Escritura.numero` es hoy un entero libre que el Escribano ingresa a mano al
preparar (CU05) o firmar (CU06) una escritura, sin ninguna validación: nada
impide guardar un número duplicado ni deja rastro de por qué se salta un
número (por ejemplo, una escritura anulada o "no pasó"). Esto expone al
protocolo del escribano a incumplir la correlatividad exigida por la
normativa notarial (CU86). El sistema ya modela, a través de `Folio`, el
escribano actuante (`fkIdPersonaEscribano`), el año (`anio`) y — desde
`protocolo-auxiliar-tramites` — si el folio pertenece al Protocolo Auxiliar
(`TipoDeFolio.esAuxiliar`); este cambio usa esos tres datos para acotar el
control de correlatividad al protocolo, año y escribano correctos, y para
tratar la numeración del Protocolo Auxiliar como una secuencia
independiente de la del Protocolo Principal, tal como exige CU86 (excepción
3.3) y ya empezó a modelar `protocolo-auxiliar-tramites`. Este cambio
implementa la última de las cinco áreas del bloque de protocolo sin
desarrollo cubierto por el Issue #839; se propone y se implementa por
separado de las otras cuatro porque, más allá del dominio "protocolo", no
comparten código entre sí (ver Technical Notes del Issue).

## What Changes

- Antes de guardar una escritura con un `numero` nuevo o modificado, el
  sistema calcula el siguiente correlativo esperado dentro del alcance
  (escribano, año, protocolo principal/auxiliar) derivado del folio
  asociado a la escritura, y compara el número propuesto contra ese
  correlativo.
- Si el número ya fue usado por otra escritura dentro del mismo alcance, el
  sistema rechaza el guardado (409) informando el duplicado.
- Si el número deja un salto respecto al correlativo esperado, el sistema
  exige una justificación (reutilizando el campo `Escritura.observaciones`
  ya existente) antes de permitir el guardado, en lugar de rechazarlo
  directamente — un salto es válido cuando corresponde a una escritura
  anulada o "no pasó" ya registrada.
- Si el número coincide con el correlativo esperado, el sistema guarda la
  escritura con normalidad, sin pedir justificación.
- `ProtocoloAuxiliarService` (de `protocolo-auxiliar-tramites`), que ya
  calculaba `MAX(numero) + 1` acotado a folios auxiliares para autoasignar
  el número al iniciar una escritura de Protocolo Auxiliar, se refactoriza
  para delegar ese cálculo en el nuevo servicio compartido de este cambio,
  evitando duplicar la misma lógica de correlatividad en dos lugares.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| El número de escritura debe ser único dentro del protocolo, año y escribano correspondientes. | CU86 (excepción 3.1) | New. |
| Un salto respecto al correlativo esperado requiere una justificación registrada. | CU86 (excepción 3.2) | New. |
| La numeración del Protocolo Auxiliar es correlativa y se controla en forma independiente de la del Protocolo Principal. | CU86 (excepción 3.3) | New (formaliza y reutiliza el cálculo ya introducido en `protocolo-auxiliar-tramites`). |

## Capabilities

### New Capabilities
- `numeracion-escrituras`: validar la correlatividad del número de
  escritura dentro del protocolo, año y escribano correspondientes,
  detectando duplicados y exigiendo justificación ante saltos.

### Modified Capabilities
- `protocolo-auxiliar` (de `protocolo-auxiliar-tramites`): el cálculo del
  siguiente correlativo auxiliar pasa a delegarse en el nuevo servicio
  compartido de este cambio; el comportamiento observable
  (`ProtocoloAuxiliarService#iniciarEscritura` asigna el siguiente
  correlativo del Protocolo Auxiliar) no cambia.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevo `NumeracionEscrituraService`, validación en `EscrituraService#save`, refactor de `ProtocoloAuxiliarService` para reutilizarlo |
| `frontend` | yes | Formulario de preparación/firma de escritura muestra el correlativo esperado y pide justificación ante un salto |
| `frontend-swing` | no | — |
| `notaire-shared` | no | Reutiliza `DtoEscritura` existente (`observaciones` ya está mapeado) |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna nueva; ningún campo nuevo (`Escritura.observaciones` ya
  existe y se reutiliza para la justificación del salto).
- Endpoints: `EscrituraController` existente (`POST`/`PUT
  /api/v1/escrituras`) gana la validación de correlatividad; sin endpoints
  nuevos.
- Database (Flyway `V{n}`): ninguna — no se agregan columnas ni tablas.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.
- No es BREAKING: agrega validación a un endpoint existente; los clientes
  que ya envían números correlativos correctos no ven cambio de
  comportamiento.

### Architecture review

Sigue la arquitectura existente: lógica de negocio en `service`, sin nuevas
entidades ni tablas. No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU86 – Controlar Numeración Correlativa de Escrituras.md` | Completar el campo `GitHub ID` con `#839` y confirmar que el flujo documentado coincide con lo implementado. |
| `CHANGELOG.md` | `[Unreleased]` — control de numeración correlativa de escrituras. |

## Out of Scope

- **Asignación automática del número en el Protocolo Principal**: a
  diferencia del Protocolo Auxiliar (donde `protocolo-auxiliar-tramites` ya
  asigna el número automáticamente), en el Protocolo Principal el Escribano
  sigue ingresando el número manualmente (CU05 paso 5); este cambio solo
  agrega la validación, no cambia ese flujo a asignación automática.
- **Subsanación de un número ya guardado incorrectamente**: este cambio
  valida al guardar; corregir un número ya persistido incorrectamente (por
  ejemplo, por datos migrados) es una operación administrativa fuera de
  alcance.
- **Las otras cuatro áreas del Issue #839** (cuadernos de folios, carpetas
  de trámite, protocolo auxiliar, minuta de inscripción) se implementan
  como cambios OpenSpec independientes: `protocolo-cuadernos-de-folios`,
  `protocolo-carpetas-de-tramite`, `protocolo-auxiliar-tramites`,
  `protocolo-minuta-inscripcion`.
