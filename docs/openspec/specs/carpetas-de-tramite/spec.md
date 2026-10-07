# Carpetas de Trámite Specification

## Purpose

Genera y administra el ciclo de vida (activa, en espera, archivada) de la
carpeta que agrupa la documentación de cada trámite dentro de una gestión
(CU85).

## Requirements

### Requirement: Generar carpeta de trámite automáticamente
El sistema SHALL generar automáticamente una carpeta de trámite con número
único, en estado "Activa", al iniciarse un trámite dentro de una gestión.

#### Scenario: Alta de un trámite único en la gestión
- **WHEN** se inicia una gestión con un único trámite
- **THEN** el sistema genera una carpeta con número único, en estado
  "Activa", vinculada a la gestión y al trámite

#### Scenario: Gestión que agrupa más de un trámite
- **WHEN** se inicia una gestión que agrupa más de un trámite
- **THEN** el sistema genera una carpeta por cada trámite, cada una con su
  propio número único, todas vinculadas a la misma gestión

### Requirement: Consultar el estado de una carpeta de trámite
El sistema SHALL permitir consultar en cualquier momento el número, estado
actual, gestión y trámite(s) asociados de una carpeta de trámite.

#### Scenario: Consulta de una carpeta existente
- **WHEN** se solicita la carpeta de un trámite dado
- **THEN** el sistema muestra número de carpeta, estado actual, gestión y
  trámite asociados

#### Scenario: Consulta de una carpeta inexistente
- **WHEN** se solicita la carpeta de un trámite que no tiene carpeta
  registrada
- **THEN** el sistema informa que no fue encontrada

### Requirement: Poner una carpeta en espera
El sistema SHALL permitir poner una carpeta de trámite en estado "Espera",
registrando el motivo.

#### Scenario: Carpeta puesta en espera con motivo
- **WHEN** se pone en espera una carpeta activa, indicando un motivo
- **THEN** el sistema cambia el estado de la carpeta a "Espera" y registra
  el motivo

#### Scenario: Intento de poner en espera sin motivo
- **WHEN** se pone en espera una carpeta sin indicar motivo
- **THEN** el sistema rechaza la solicitud y no cambia el estado de la
  carpeta

### Requirement: Archivar las carpetas al archivar la gestión
El sistema SHALL cambiar a "Archivada" el estado de cada carpeta de trámite
asociada a una gestión cuando esa gestión se archiva (CU16), alertando y
exigiendo confirmación explícita si alguna carpeta sigue en estado "Espera"
sin motivo resuelto.

#### Scenario: Archivado de gestión con todas las carpetas activas
- **WHEN** se archiva una gestión cuyas carpetas de trámite están en estado
  "Activa"
- **THEN** el sistema cambia el estado de todas esas carpetas a "Archivada"

#### Scenario: Archivado de gestión con una carpeta en espera sin resolver
- **WHEN** se intenta archivar una gestión que tiene una carpeta en estado
  "Espera" con el motivo aún sin resolver
- **THEN** el sistema alerta de la situación y exige confirmación explícita
  antes de archivar

#### Scenario: Confirmación explícita de archivado con carpeta en espera
- **WHEN** se confirma explícitamente el archivado de una gestión con una
  carpeta en estado "Espera" sin resolver
- **THEN** el sistema archiva la gestión y cambia esa carpeta a "Archivada"
