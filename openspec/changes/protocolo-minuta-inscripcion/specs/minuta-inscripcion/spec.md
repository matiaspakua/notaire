## Purpose

Genera la Minuta de Inscripción para escrituras sobre inmuebles y hace
seguimiento de su circuito registral (presentación, observaciones,
inscripción definitiva) ante el Registro de la Propiedad Inmueble (CU82).

## ADDED Requirements

### Requirement: Registrar datos registrales del inmueble
El sistema SHALL permitir registrar la matrícula, tomo/folio/finca y
linderos de un inmueble.

#### Scenario: Cargar datos registrales de un inmueble
- **WHEN** se completan la matrícula, el tomo/folio/finca y los linderos de
  un inmueble
- **THEN** el sistema guarda esos datos junto con la nomenclatura catastral
  y la valuación fiscal existentes

### Requirement: Generar la Minuta de Inscripción
El sistema SHALL permitir generar la Minuta de Inscripción para una escritura
sobre un inmueble con trámite aprobado, siempre que estén completos los
datos catastrales y registrales requeridos.

#### Scenario: Generar minuta con datos completos
- **WHEN** se solicita generar la minuta de inscripción para una escritura
  sobre un inmueble cuyo trámite está aprobado y cuyos datos catastrales y
  registrales están completos
- **THEN** el sistema genera la minuta con un número identificador, en
  estado "Generada"

#### Scenario: Intento de generar minuta con datos incompletos
- **WHEN** se solicita generar la minuta de inscripción para un inmueble al
  que le faltan datos catastrales o registrales requeridos
- **THEN** el sistema rechaza la solicitud informando los campos faltantes y
  no genera la minuta

#### Scenario: Imprimir la minuta en formulario normalizado
- **WHEN** se solicita el documento de una minuta ya generada
- **THEN** el sistema devuelve el reporte en formato PDF con el formulario
  normalizado

### Requirement: Presentar la minuta ante el Registro
El sistema SHALL permitir registrar la presentación de la minuta ante el
Registro de la Propiedad Inmueble.

#### Scenario: Registrar presentación
- **WHEN** se registra la presentación de una minuta generada, indicando la
  fecha y el número de entrada registral
- **THEN** el sistema guarda esos datos y cambia el estado de la minuta a
  "Presentado para inscripción"

### Requirement: Registrar observaciones del Registro
El sistema SHALL permitir registrar observaciones formuladas por el Registro
sobre una minuta presentada.

#### Scenario: Registrar observación
- **WHEN** el Registro formula observaciones sobre una minuta presentada
- **THEN** el sistema guarda las observaciones y la fecha de subsanación, y
  cambia el estado de la minuta a "Observado"

### Requirement: Registrar la inscripción definitiva
El sistema SHALL permitir registrar la inscripción definitiva de una minuta
presentada.

#### Scenario: Registrar inscripción definitiva
- **WHEN** se registra la devolución del testimonio inscripto, indicando la
  fecha de recepción y el número de inscripción definitivo
- **THEN** el sistema guarda esos datos y cambia el estado de la minuta a
  "Inscripto"
