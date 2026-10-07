# Gestión de trámites en protocolo auxiliar

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #839 |
| Use Case | CU81 – Gestión de Trámites en Protocolo Auxiliar |
| Branch | `feat/839_protocolo-auxiliar-tramites` |
| Gate 1 status | pending |

## Objetivo

El sistema no distingue folios de Protocolo Principal de folios de Protocolo
Auxiliar (`TipoDeFolio.nombre` es texto libre sin ninguna marca), por lo que
no existe forma de listar los folios auxiliares disponibles ni de asignarles
una numeración de escritura correlativa propia, independiente de la del
protocolo principal (CU81). Como consecuencia, certificaciones de firma,
actas y poderes — el circuito ágil de 5 pasos sin apertura de carpeta — no
tienen ningún soporte en el sistema hoy. Este cambio implementa una de las
cinco áreas del bloque de protocolo sin desarrollo cubierto por el Issue
#839; se propone y se implementa por separado de las otras cuatro porque, más
allá del dominio "protocolo", no comparten código entre sí (ver Technical
Notes del Issue).

## What Changes

- `TipoDeFolio` gana un campo `esAuxiliar` (boolean, default `false`) que
  distingue los tipos de folio de Protocolo Auxiliar de los de Protocolo
  Principal. Esta distinción es la base que reutilizará también
  `protocolo-numeracion-escrituras` (CU86) para separar la correlatividad de
  escrituras por protocolo.
- Nuevo endpoint para listar los folios de Protocolo Auxiliar disponibles
  (`fkIdTipoFolio.esAuxiliar = true`, sin escritura asociada).
- Nuevo endpoint para iniciar una escritura en Protocolo Auxiliar: valida que
  haya folio auxiliar disponible, asigna el siguiente número de escritura
  correlativo **dentro del Protocolo Auxiliar** (numeración independiente de
  la del Protocolo Principal), y vincula la escritura al folio auxiliar
  elegido y al cliente.
- La escritura de Protocolo Auxiliar se crea sobre un `Tramite` sin gestión
  asociada (`fkIdGestion = null`): como consecuencia directa, y de forma
  consistente con `protocolo-carpetas-de-tramite`, **no se genera carpeta de
  trámite** para este circuito, tal como exige CU81 (sin necesitar ningún caso
  especial en la generación automática de carpetas).
- Reutiliza, sin modificarlos, los pasos ya existentes o cubiertos por otros
  cambios: alta rápida de cliente (CU17/CU18), firma de escritura (CU06),
  generación de testimonio (CU07) y marca de folio "no pasó" ante escritura
  sin firmar (CU33/RF #98).
- Nueva pantalla de Protocolo Auxiliar: listar folios auxiliares disponibles,
  iniciar la escritura, y desde ahí continuar hacia firma/testimonio/entrega
  con las pantallas ya existentes de esos flujos.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un tipo de folio puede marcarse como perteneciente al Protocolo Auxiliar. | CU81 (paso 3), RF #119 | New. |
| Solo pueden iniciarse escrituras de Protocolo Auxiliar sobre folios de tipo auxiliar disponibles (sin escritura asociada). | CU81 (paso 3, excepción 3.1) | New. |
| La numeración de escrituras del Protocolo Auxiliar es correlativa y separada de la numeración del Protocolo Principal. | CU81 (paso 3), RF #112 | New. |
| Si no hay folios de Protocolo Auxiliar disponibles, el sistema informa la falta y no permite iniciar la escritura. | CU81 (excepción 3.1) | New. |
| La escritura de Protocolo Auxiliar no genera carpeta de trámite física. | CU81 (paso 7), RF #111 | New. |

## Capabilities

### New Capabilities
- `protocolo-auxiliar`: distinguir folios de Protocolo Auxiliar, listar los
  disponibles e iniciar escrituras con numeración correlativa propia y sin
  carpeta de trámite.

### Modified Capabilities
(ninguna — `carpetas-de-tramite`, introducida en `protocolo-carpetas-de-tramite`,
no requiere ningún cambio de comportamiento: al no tener gestión asociada, un
trámite de Protocolo Auxiliar simplemente no dispara su generación
automática, sin necesitar ninguna excepción explícita en esa capability.)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Campo `esAuxiliar` en `TipoDeFolio`, nuevo `ProtocoloAuxiliarController`, `ProtocoloAuxiliarService` |
| `frontend` | yes | Nueva pantalla de Protocolo Auxiliar |
| `frontend-swing` | no | — |
| `notaire-shared` | yes | `DtoTipoDeFolio` gana `esAuxiliar`; nuevo `DtoEscrituraProtocoloAuxiliar` (o reutiliza `DtoEscritura` con folio embebido — a decidir en design.md) |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `TipoDeFolio` (agrega `esAuxiliar`); ninguna entidad nueva —
  reutiliza `Escritura`, `Folio` y `Tramite` existentes.
- Endpoints: `GET /api/v1/protocolo-auxiliar/folios-disponibles`, `POST
  /api/v1/protocolo-auxiliar/escrituras`.
- Database (Flyway `V{n}`): nueva columna `es_auxiliar` (boolean, default
  `false`) en `tipos_folio`.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.
- No es BREAKING: `TipoDeFolio` gana un campo con default `false`, no se
  modifica ningún contrato existente.

### Architecture review

Sigue la arquitectura existente: repositorio en `repository`, controller REST
en `api`, lógica de negocio en `service`. No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU81 – Gestión de Trámites en Protocolo Auxiliar.md` | Confirmar que el flujo documentado coincide con lo implementado. |
| `CHANGELOG.md` | `[Unreleased]` — gestión de trámites en Protocolo Auxiliar. |

## Out of Scope

- **Firma, testimonio y entrega**: este cambio crea la escritura de Protocolo
  Auxiliar sobre un folio auxiliar con numeración propia; los pasos de firma
  (CU06), generación de testimonio (CU07) y su circuito posterior son los ya
  cubiertos (o en desarrollo) por `escritura-post-firma-legal-cycle` (#832) —
  no se reimplementan aquí.
- **Nueva pantalla de administración de tipos de folio**: ya existe
  `frontend/src/app/dashboard/administracion/folios/page.tsx`; este cambio
  agrega el checkbox `esAuxiliar` a ese formulario existente, no crea una
  pantalla nueva de administración de tipos de folio.
- **Las otras cuatro áreas del Issue #839** (cuadernos de folios, carpetas de
  trámite, minuta de inscripción, numeración de escrituras) se implementan
  como cambios OpenSpec independientes: `protocolo-cuadernos-de-folios`,
  `protocolo-carpetas-de-tramite`, `protocolo-minuta-inscripcion`,
  `protocolo-numeracion-escrituras`.
