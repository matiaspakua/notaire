## Purpose

Permite agrupar los folios notariales en cuadernos correlativos de diez
folios, tal como exige la normativa del protocolo, y emitir la carátula
oficial de cada cuaderno (CU80).

## ADDED Requirements

### Requirement: Generar cuaderno a partir de un rango de folios
El sistema SHALL permitir generar un cuaderno agrupando exactamente diez
folios consecutivos de un mismo registro notarial.

#### Scenario: Alta de cuaderno con rango válido
- **WHEN** se solicita generar un cuaderno indicando diez folios disponibles,
  consecutivos y de un mismo registro notarial
- **THEN** el sistema crea el cuaderno, le asigna el siguiente número
  correlativo disponible para ese registro y año, y agrupa los diez folios

#### Scenario: Cantidad de folios no múltiplo de diez
- **WHEN** se solicita generar un cuaderno indicando una cantidad de folios
  que no es múltiplo exacto de diez
- **THEN** el sistema rechaza la solicitud y no crea ningún cuaderno

#### Scenario: Rango de folios discontinuo
- **WHEN** se solicita generar un cuaderno indicando folios con faltantes o
  numeración no consecutiva
- **THEN** el sistema rechaza la solicitud, informando la discontinuidad, y
  no crea ningún cuaderno

#### Scenario: Folio ya asignado a otro cuaderno
- **WHEN** se solicita generar un cuaderno incluyendo un folio que ya está
  asignado a otro cuaderno
- **THEN** el sistema rechaza la solicitud y no crea ningún cuaderno

#### Scenario: Lote con folio dañado o anulado justificado
- **WHEN** se solicita generar un cuaderno cuyo rango incluye un folio en
  estado dañado o anulado, junto con una justificación para ese folio
- **THEN** el sistema crea el cuaderno incluyendo el folio afectado, con la
  justificación registrada como observación del cuaderno

### Requirement: Marcar los folios como asignados a cuaderno
El sistema SHALL actualizar el estado de cada folio agrupado a "Asignado a
cuaderno" al generarse el cuaderno que lo contiene.

#### Scenario: Estado de folio tras generar el cuaderno
- **WHEN** un cuaderno se genera exitosamente
- **THEN** cada uno de sus diez folios queda con estado "Asignado a cuaderno"

### Requirement: Numerar cuadernos correlativamente por registro y año
El sistema SHALL asignar a cada cuaderno un número correlativo dentro de su
registro notarial y año, sin duplicados.

#### Scenario: Primer cuaderno del año para un registro
- **WHEN** se genera el primer cuaderno de un registro notarial en un año
  determinado
- **THEN** el cuaderno recibe el número 1

#### Scenario: Conflicto de numeración
- **WHEN** se genera un nuevo cuaderno para un registro y año que ya tienen
  cuadernos numerados, y el número esperado ya está tomado
- **THEN** el sistema recalcula y asigna el siguiente número correlativo
  disponible, sin requerir intervención manual

### Requirement: Emitir la carátula oficial de un cuaderno
El sistema SHALL generar la carátula oficial de un cuaderno, indicando año,
registro del escribano, número de cuaderno, rango de folios y el detalle de
las escrituras o trámites otorgados en esos folios.

#### Scenario: Emisión de carátula de un cuaderno existente
- **WHEN** se solicita la carátula de un cuaderno ya generado
- **THEN** el sistema entrega un documento con año, registro del escribano,
  número de cuaderno, rango de folios y el detalle de escrituras/trámites
  otorgados en los folios que lo componen

#### Scenario: Emisión de carátula de un cuaderno inexistente
- **WHEN** se solicita la carátula de un cuaderno que no existe
- **THEN** el sistema rechaza la solicitud indicando que el cuaderno no fue
  encontrado
