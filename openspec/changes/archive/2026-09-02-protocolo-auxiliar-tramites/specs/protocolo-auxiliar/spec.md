## Purpose

Distingue los folios de Protocolo Auxiliar de los de Protocolo Principal y
permite iniciar escrituras de Protocolo Auxiliar con numeración correlativa
propia y sin generación de carpeta de trámite (CU81).

## ADDED Requirements

### Requirement: Distinguir tipos de folio de Protocolo Auxiliar
El sistema SHALL permitir marcar un tipo de folio como perteneciente al
Protocolo Auxiliar.

#### Scenario: Marcar un tipo de folio como auxiliar
- **WHEN** se marca un tipo de folio como perteneciente al Protocolo
  Auxiliar
- **THEN** el sistema guarda la marca y ese tipo queda disponible para
  Protocolo Auxiliar

#### Scenario: Tipo de folio sin marcar es de Protocolo Principal
- **WHEN** se consulta un tipo de folio que no fue marcado como auxiliar
- **THEN** el sistema lo considera parte del Protocolo Principal

### Requirement: Listar folios de Protocolo Auxiliar disponibles
El sistema SHALL permitir listar los folios de tipo auxiliar que no tienen
escritura asociada.

#### Scenario: Hay folios auxiliares disponibles
- **WHEN** se solicita el listado de folios de Protocolo Auxiliar
  disponibles y existen folios de tipo auxiliar sin escritura asociada
- **THEN** el sistema devuelve esos folios

#### Scenario: No hay folios auxiliares disponibles
- **WHEN** se solicita el listado de folios de Protocolo Auxiliar
  disponibles y no existe ninguno sin escritura asociada
- **THEN** el sistema informa la falta de folios auxiliares disponibles

### Requirement: Iniciar escritura de Protocolo Auxiliar con numeración propia
El sistema SHALL permitir iniciar una escritura de Protocolo Auxiliar sobre
un folio auxiliar disponible, asignándole el siguiente número correlativo
dentro de la numeración del Protocolo Auxiliar, independiente de la
numeración del Protocolo Principal.

#### Scenario: Alta de escritura de Protocolo Auxiliar con folio disponible
- **WHEN** se inicia una escritura de Protocolo Auxiliar indicando un folio
  auxiliar disponible y un cliente
- **THEN** el sistema crea la escritura, le asigna el siguiente número
  correlativo del Protocolo Auxiliar y la vincula al folio elegido

#### Scenario: Intento de iniciar escritura sin folio auxiliar disponible
- **WHEN** se intenta iniciar una escritura de Protocolo Auxiliar y no hay
  ningún folio auxiliar disponible
- **THEN** el sistema rechaza la solicitud informando la falta de folios y
  no crea ninguna escritura

#### Scenario: Numeración del Protocolo Auxiliar independiente del Principal
- **WHEN** se inicia una nueva escritura de Protocolo Auxiliar y ya existen
  escrituras numeradas en el Protocolo Principal
- **THEN** el número correlativo asignado continúa la secuencia del
  Protocolo Auxiliar, sin verse afectado por la numeración del Protocolo
  Principal

### Requirement: No generar carpeta de trámite en Protocolo Auxiliar
El sistema SHALL iniciar la escritura de Protocolo Auxiliar sin generar
carpeta de trámite.

#### Scenario: Escritura de Protocolo Auxiliar sin carpeta
- **WHEN** se inicia una escritura de Protocolo Auxiliar
- **THEN** el sistema no genera ninguna carpeta de trámite asociada
