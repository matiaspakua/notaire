# minuta-inscripcion Delta Specification

## MODIFIED Requirements

### Requirement: Generar la Minuta de Inscripción

El sistema SHALL permitir generar la Minuta de Inscripción para una escritura
sobre un inmueble con trámite aprobado, siempre que estén completos los
datos catastrales y registrales requeridos. A successful generate via
`POST /api/v1/minutas-inscripcion` SHALL return HTTP `201 Created` with a
`Location` header pointing at `/api/v1/minutas-inscripcion/{id}`.

#### Scenario: Generar minuta con datos completos

- **WHEN** se solicita generar la minuta de inscripción para una escritura
  sobre un inmueble cuyo trámite está aprobado y cuyos datos catastrales y
  registrales están completos
- **THEN** el sistema genera la minuta con un número identificador, en
  estado "Generada", responde `201 Created` e incluye `Location` de la minuta

#### Scenario: Intento de generar minuta con datos incompletos

- **WHEN** se solicita generar la minuta de inscripción para un inmueble al
  que le faltan datos catastrales o registrales requeridos
- **THEN** el sistema rechaza la solicitud informando los campos faltantes y
  no genera la minuta

#### Scenario: Imprimir la minuta en formulario normalizado

- **WHEN** se solicita el documento de una minuta ya generada
- **THEN** el sistema devuelve el reporte en formato PDF con el formulario
  normalizado
