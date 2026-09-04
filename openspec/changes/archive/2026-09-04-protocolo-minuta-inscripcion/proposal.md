# Generar y hacer seguimiento de la Minuta de Inscripción

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #839 |
| Use Case | CU82 – Generar Minuta de Inscripción |
| Branch | `feat/839_protocolo-minuta-inscripcion` |
| Gate 1 status | pending |

## Objetivo

Cuando una escritura constituye, transmite o modifica derechos reales sobre un
inmueble (compraventa, donación, hipoteca), el Escribano debe emitir una
"Minuta de Inscripción" con los datos catastrales y registrales del inmueble,
presentarla junto con el testimonio ante el Registro de la Propiedad Inmueble,
y hacer seguimiento de su circuito (presentación, observaciones, devolución e
inscripción definitiva). Hoy el sistema no soporta ninguno de estos pasos: la
entidad `Inmueble` no tiene los campos registrales que la minuta exige
(matrícula, tomo/folio/finca, linderos) — solo tiene nomenclatura catastral y
valuación fiscal — y no existe ninguna forma de generar el documento, ni de
registrar su presentación, observación o inscripción definitiva (CU82). Este
cambio implementa una de las cinco áreas del bloque de protocolo sin
desarrollo cubierto por el Issue #839; se propone y se implementa por
separado de las otras cuatro porque, más allá del dominio "protocolo", no
comparten código entre sí (ver Technical Notes del Issue).

## What Changes

- `Inmueble` gana los campos registrales permanentes que le faltan y que la
  minuta exige mostrar: `matricula`, `tomoFolioFinca` y `linderos`. Estos
  datos identifican al inmueble ante el Registro y no cambian entre
  escrituras, a diferencia de los datos que sí varían por operación (precio,
  partes), por lo que se modelan sobre `Inmueble` y no sobre la minuta.
- Nueva entidad `MinutaInscripcion`, una por `Escritura` que involucra un
  inmueble, que modela el circuito registral completo: generación (con
  número identificador y precio de la operación), presentación (fecha y
  número de entrada registral), observación (observaciones del registro y
  fecha de subsanación/reingreso) e inscripción definitiva (fecha de
  recepción y número de inscripción).
- Nuevo endpoint para generar la minuta desde una escritura con trámite de
  inmueble y estado aprobado: valida que estén completos los datos
  catastrales/registrales requeridos (paso 2, excepción 2.1) antes de
  permitir la generación.
- Nuevos endpoints para avanzar el circuito: marcar como presentada ante el
  Registro, registrar observaciones (vuelve a "Observado", habilitando su
  reingreso — CU44 reutilizado sin reimplementar), y registrar la
  inscripción definitiva.
- Reporte JasperReports de la Minuta de Inscripción en el "formulario
  normalizado" exigido por la autoridad registral, siguiendo el patrón ya
  existente en `ReporteController`.
- Nueva pantalla para generar la minuta desde una escritura, ver/editar los
  datos catastrales del inmueble asociado, y hacer seguimiento de su estado
  (Generada → Presentada → Observada / Inscripta).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Solo puede generarse la minuta para una gestión de inmuebles con escritura aprobada. | CU82 (paso 1) | New. |
| No puede generarse la minuta si faltan datos catastrales/registrales requeridos. | CU82 (excepción 2.1) | New. |
| La minuta generada puede imprimirse en formulario normalizado. | CU82 (paso 3), RF #117 | New. |
| Al presentarse ante el Registro se registra fecha de presentación y número de entrada registral. | CU82 (paso 4), RF #118 | New. |
| Si el Registro formula observaciones, la minuta pasa a estado "Observado" con fecha de subsanación. | CU82 (excepción 5.1) | New. |
| Al recibir el testimonio inscripto se registra fecha de recepción, número de inscripción definitivo y observaciones del registro. | CU82 (paso 5) | New. |

## Capabilities

### New Capabilities
- `minuta-inscripcion`: generar la Minuta de Inscripción desde una escritura
  sobre inmueble y hacer seguimiento de su circuito registral hasta la
  inscripción definitiva.

### Modified Capabilities
(ninguna — `Inmueble` gana campos aditivos, sin cambiar comportamiento
existente.)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Campos nuevos en `Inmueble`, nueva entidad `MinutaInscripcion`, `MinutaInscripcionController`, `MinutaInscripcionService`, nuevo reporte en `ReporteController` |
| `frontend` | yes | Nueva pantalla de Minuta de Inscripción |
| `frontend-swing` | no | — |
| `notaire-shared` | yes | `DtoInmueble` gana `matricula`/`tomoFolioFinca`/`linderos`; nuevo `DtoMinutaInscripcion` |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Inmueble` (agrega `matricula`, `tomoFolioFinca`, `linderos`);
  nueva entidad `MinutaInscripcion` (`fkIdEscritura` uno-a-uno, `numero`,
  `precioOperacion`, `estado`, `fechaPresentacion`,
  `numeroEntradaRegistral`, `fechaRecepcion`, `numeroInscripcionDefinitivo`,
  `observacionesRegistro`, `fechaSubsanacion`).
- Endpoints: `POST /api/v1/minutas-inscripcion`,
  `PUT /api/v1/minutas-inscripcion/{id}/presentar`,
  `PUT /api/v1/minutas-inscripcion/{id}/observar`,
  `PUT /api/v1/minutas-inscripcion/{id}/inscribir`,
  `GET /api/v1/reportes/minuta-inscripcion/{id}`.
- Database (Flyway `V{n}`): columnas `matricula`, `tomo_folio_finca`,
  `linderos` en `inmuebles`; nueva tabla `minutas_inscripcion`.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.
- No es BREAKING: `Inmueble` gana campos opcionales; `MinutaInscripcion` es
  una entidad nueva sin relación con contratos existentes.

### Architecture review

Sigue la arquitectura existente: repositorio en `repository`, controller
REST en `api`, lógica de negocio en `service`, reporte en `ReporteController`
(JasperReports). No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` | Confirmar que el flujo documentado coincide con lo implementado. |
| `CHANGELOG.md` | `[Unreleased]` — generación y seguimiento de la Minuta de Inscripción. |

## Out of Scope

- **Subsanación y reingreso del testimonio (CU44)**: este cambio registra el
  estado "Observado" y la fecha de subsanación, pero el circuito de
  corrección y reingreso del testimonio en sí es CU44, ya cubierto (o en
  desarrollo) por otro cambio — no se reimplementa aquí.
- **Firma y generación de testimonio (CU05/CU07)**: prerequisitos de este
  circuito, reutilizados sin modificarlos.
- **Datos catastrales de terceros (CU69)**: la consulta de datos catastrales
  externos referenciada por CU82 paso 1 no es parte de este cambio; se
  asume que los datos catastrales ya están cargados en `Inmueble`.
- **Las otras cuatro áreas del Issue #839** (cuadernos de folios, carpetas de
  trámite, protocolo auxiliar, numeración de escrituras) se implementan como
  cambios OpenSpec independientes: `protocolo-cuadernos-de-folios`,
  `protocolo-carpetas-de-tramite`, `protocolo-auxiliar-tramites`,
  `protocolo-numeracion-escrituras`.
