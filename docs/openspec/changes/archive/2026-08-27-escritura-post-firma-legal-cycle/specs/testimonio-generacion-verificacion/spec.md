<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Provides the business actions to generate a testimonio from a signed
escritura (CU07), verify it (CU08), and issue its printed copy (RF-94).

## ADDED Requirements

### Requirement: Generar testimonio a partir de escritura firmada
El sistema SHALL permitir generar un testimonio solo a partir de una
escritura en estado "Firmada", según CU07 y RF-31. SHALL rechazar la
generación si la escritura de origen no está firmada.

#### Scenario: Generación exitosa desde escritura firmada
- **WHEN** el Escribano genera un testimonio a partir de una escritura en
  estado "Firmada"
- **THEN** el sistema crea el testimonio vinculado a esa escritura y
  responde con el testimonio creado

#### Scenario: Rechazo de generación desde escritura no firmada
- **WHEN** el Escribano intenta generar un testimonio a partir de una
  escritura que no está en estado "Firmada"
- **THEN** el sistema rechaza la operación sin crear el testimonio y
  responde con un error que indica que la escritura no está firmada

### Requirement: Verificar testimonio generado
El sistema SHALL permitir verificar un testimonio ya generado, registrando
si quedó observado y, de ser así, el motivo de la observación, según CU08.

#### Scenario: Verificación sin observaciones
- **WHEN** el Escribano verifica un testimonio generado y no reporta
  observaciones
- **THEN** el sistema marca el testimonio como verificado y no observado

#### Scenario: Verificación con observaciones
- **WHEN** el Escribano verifica un testimonio generado y reporta un
  motivo de observación
- **THEN** el sistema marca el testimonio como verificado y observado,
  registrando el motivo informado

#### Scenario: Rechazo de verificación de testimonio inexistente
- **WHEN** se intenta verificar un testimonio cuyo identificador no existe
- **THEN** el sistema rechaza la operación y responde con un error de
  recurso no encontrado

### Requirement: Emitir copia impresa del testimonio verificado
El sistema SHALL permitir emitir una copia impresa (PDF) de un testimonio
solo después de que haya sido verificado, según RF-94, reutilizando el
patrón de generación de reportes ya existente.

#### Scenario: Emisión de copia PDF de testimonio verificado
- **WHEN** el Escribano solicita la copia impresa de un testimonio ya
  verificado
- **THEN** el sistema genera y devuelve el documento PDF de la copia

#### Scenario: Rechazo de emisión de copia de testimonio no verificado
- **WHEN** el Escribano solicita la copia impresa de un testimonio que
  todavía no fue verificado
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el testimonio no está verificado
