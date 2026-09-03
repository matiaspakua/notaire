# costos-documentos-presupuesto Specification

## Purpose

Conecta el costo de un documento presentado (sello, impuesto) al total del
presupuesto de su trámite, y permite definir gastos fijos o variables
esperados por tipo de documento en la plantilla de presupuesto de un tipo
de trámite (CU27, CU39).

## Requirements

### Requirement: Incluir el costo de los documentos en el total del presupuesto
El sistema SHALL sumar el costo (`importeAPagar`) de los documentos
presentados asociados a los trámites de un presupuesto al calcular su
total.

#### Scenario: Presupuesto con un documento con costo asociado
- **WHEN** un presupuesto tiene un trámite con un documento presentado cuyo
  costo es mayor a cero
- **THEN** el total del presupuesto incluye ese costo

#### Scenario: Presupuesto con varios documentos con costo
- **WHEN** un presupuesto tiene un trámite con varios documentos
  presentados, cada uno con un costo asociado
- **THEN** el total del presupuesto incluye la suma de todos esos costos

#### Scenario: Presupuesto sin documentos con costo
- **WHEN** un presupuesto no tiene ningún documento presentado con costo
  asociado
- **THEN** el total del presupuesto se calcula igual que hoy, sin ningún
  costo adicional

### Requirement: Definir gastos fijos o variables por tipo de documento en la plantilla
El sistema SHALL permitir definir, para un tipo de trámite, un gasto fijo o
variable esperado asociado a un tipo de documento.

#### Scenario: Definir un gasto fijo por tipo de documento
- **WHEN** se define en la plantilla de presupuesto de un tipo de trámite
  un gasto fijo para un tipo de documento
- **THEN** el sistema acepta y persiste esa definición

#### Scenario: Definir un gasto variable por tipo de documento
- **WHEN** se define en la plantilla de presupuesto de un tipo de trámite
  un gasto variable (porcentaje) para un tipo de documento
- **THEN** el sistema acepta y persiste esa definición

#### Scenario: Consultar los gastos por tipo de documento de una plantilla
- **WHEN** se consulta la plantilla de presupuesto de un tipo de trámite
  que tiene gastos definidos por tipo de documento
- **THEN** el sistema responde con esos gastos
