# testimonio-movimiento-inscripcion Specification

## Purpose

Provides the movement circuit of a testimonio through the property
registry: presenting it for inscription (CU11), recording the inscription,
withdrawing it (CU12), and re-entering it (CU44).

## Requirements

### Requirement: Ingresar testimonio para inscripción
El sistema SHALL permitir registrar el ingreso de un testimonio verificado
al registro de la propiedad para su inscripción, guardando la fecha de
ingreso, según CU11 y RF-30/RF-92.

#### Scenario: Ingreso exitoso registra fecha de ingreso
- **WHEN** el Escribano presenta un testimonio verificado para inscripción
- **THEN** el sistema registra la fecha de ingreso en el movimiento del
  testimonio

#### Scenario: Rechazo de ingreso de testimonio ya ingresado sin retirar
- **WHEN** el Escribano intenta presentar para inscripción un testimonio
  que ya tiene un movimiento con fecha de ingreso registrada y sin fecha de
  salida
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el testimonio ya está en trámite de inscripción

#### Scenario: Rechazo de ingreso de testimonio no verificado
- **WHEN** el Escribano intenta presentar para inscripción un testimonio
  que todavía no fue verificado
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el testimonio debe estar verificado

### Requirement: Registrar inscripción del testimonio
El sistema SHALL permitir marcar un testimonio ingresado como inscripto,
registrando la fecha de inscripción, según RF-32.

#### Scenario: Registro exitoso marca inscripto con fecha
- **WHEN** el Escribano registra la inscripción de un testimonio que tiene
  fecha de ingreso registrada y aún no fue marcado inscripto
- **THEN** el sistema marca el testimonio como inscripto y registra la
  fecha de inscripción

#### Scenario: Rechazo de registrar inscripción sin ingreso previo
- **WHEN** el Escribano intenta registrar la inscripción de un testimonio
  que no tiene ningún movimiento con fecha de ingreso registrada
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que falta el ingreso a inscripción

### Requirement: Retirar testimonio inscripto
El sistema SHALL permitir retirar un testimonio ya inscripto, registrando
la fecha de salida y el número de cartón, según CU12 y RF-33.

#### Scenario: Retiro exitoso registra fecha de salida y número de cartón
- **WHEN** el Escribano retira un testimonio marcado como inscripto,
  informando el número de cartón
- **THEN** el sistema registra la fecha de salida y el número de cartón en
  el movimiento del testimonio

#### Scenario: Rechazo de retiro de testimonio no inscripto
- **WHEN** el Escribano intenta retirar un testimonio que todavía no fue
  marcado como inscripto
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el testimonio no está inscripto

### Requirement: Reingresar testimonio retirado
El sistema SHALL permitir reingresar un testimonio ya retirado, generando
un nuevo movimiento sin perder el historial del movimiento anterior, según
CU44 y RF-33.

#### Scenario: Reingreso exitoso crea nuevo movimiento preservando historial
- **WHEN** el Escribano reingresa un testimonio cuyo movimiento más
  reciente tiene fecha de salida registrada
- **THEN** el sistema crea un nuevo movimiento de testimonio con su propia
  fecha de ingreso, conservando el movimiento anterior sin modificarlo

#### Scenario: Rechazo de reingreso de testimonio no retirado previamente
- **WHEN** el Escribano intenta reingresar un testimonio cuyo movimiento
  más reciente no tiene fecha de salida registrada
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el testimonio no fue retirado
