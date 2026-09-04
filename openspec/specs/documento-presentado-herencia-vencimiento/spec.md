# documento-presentado-herencia-vencimiento Specification

## Purpose

Hace que un documento presentado herede el vencimiento y el responsable de su
tipo de documento al crearse, en lugar de forzarlos siempre a valores vacíos,
resolviendo el bloqueo de datos que impide "informar próximos vencimientos"
(CU42).

## Requirements

### Requirement: Heredar vencimiento y responsable del tipo de documento
El sistema SHALL copiar `vence`, `diasVencimiento` y `quienEntrega` del
`TipoDeDocumento` asociado al crear un documento presentado, en lugar de
asignar siempre `vence = false` y `quienEntrega = ""`.

#### Scenario: Alta de documento presentado de un tipo que vence
- **WHEN** se crea un documento presentado a partir de un tipo de documento
  con `vence = true`, `diasVencimiento` y `quienEntrega` cargados
- **THEN** el documento presentado se guarda con esos mismos valores de
  `vence`, `diasVencimiento` y `quienEntrega`

#### Scenario: Alta de documento presentado de un tipo que no vence
- **WHEN** se crea un documento presentado a partir de un tipo de documento
  con `vence = false`
- **THEN** el documento presentado se guarda con `vence = false` y sin
  `fechaVencimiento`

### Requirement: Calcular la fecha de vencimiento al crear el documento
El sistema SHALL calcular `fechaVencimiento` como `fechaIngreso` más
`diasVencimiento` cuando el tipo de documento asociado tiene `vence = true` y
`fechaIngreso` fue informada.

#### Scenario: Cálculo de fecha de vencimiento
- **WHEN** se crea un documento presentado con `fechaIngreso` informada, a
  partir de un tipo de documento con `vence = true` y `diasVencimiento`
  cargado
- **THEN** el documento presentado se guarda con `fechaVencimiento` igual a
  `fechaIngreso` más `diasVencimiento` días

#### Scenario: Sin fecha de ingreso no hay fecha de vencimiento
- **WHEN** se crea un documento presentado sin `fechaIngreso`, a partir de un
  tipo de documento con `vence = true`
- **THEN** el documento presentado se guarda sin `fechaVencimiento`
