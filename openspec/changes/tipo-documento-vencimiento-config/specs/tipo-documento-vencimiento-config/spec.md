<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Permite cargar y editar, desde la pantalla de administración, si un tipo de
documento vence, en cuántos días y quién es responsable de entregarlo,
completando datos que ya existen en el modelo pero que hoy son imposibles de
ingresar (CU27, CU32).

## ADDED Requirements

### Requirement: Cargar vencimiento y responsable al crear un tipo de documento
El sistema SHALL permitir ingresar, al dar de alta un tipo de documento, si
vence (`vence`), la cantidad de días de vigencia (`diasVencimiento`) y quién
es responsable de entregarlo o devolverlo (`quienEntrega`), según CU27.

#### Scenario: Alta de tipo de documento que vence
- **WHEN** un usuario da de alta un tipo de documento indicando que vence, con
  una cantidad de días de vigencia y un responsable
- **THEN** el sistema guarda el tipo de documento con `vence = true`, el
  `diasVencimiento` indicado y el `quienEntrega` indicado

#### Scenario: Alta de tipo de documento que no vence
- **WHEN** un usuario da de alta un tipo de documento indicando que no vence
- **THEN** el sistema guarda el tipo de documento con `vence = false`, sin
  exigir `diasVencimiento`

### Requirement: Modificar vencimiento y responsable de un tipo de documento
El sistema SHALL permitir modificar `vence`, `diasVencimiento` y
`quienEntrega` de un tipo de documento existente que no esté en uso, según
CU32.

#### Scenario: Modificación de vencimiento y responsable
- **WHEN** un usuario modifica un tipo de documento existente que no está en
  uso, cambiando `vence`, `diasVencimiento` o `quienEntrega`
- **THEN** el sistema guarda los nuevos valores

#### Scenario: Modificación bloqueada por tipo de documento en uso
- **WHEN** un usuario intenta modificar un tipo de documento que ya está en
  uso (referenciado por una plantilla de trámite o un documento presentado)
- **THEN** el sistema rechaza la modificación con un mensaje indicando que
  debe crear un tipo de documento nuevo
