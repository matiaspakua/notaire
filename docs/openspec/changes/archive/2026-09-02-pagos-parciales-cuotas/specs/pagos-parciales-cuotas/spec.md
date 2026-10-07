<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Permite registrar pagos parciales sobre un presupuesto sin exigir que un
único pago cubra el saldo total, y distingue explícitamente si un
presupuesto está sin pagos, parcialmente abonado o saldado al consultarlo
(CU15, CU47).

## ADDED Requirements

### Requirement: Registrar un pago sin exigir que cubra el saldo total
El sistema SHALL aceptar el registro de un pago sobre un presupuesto cuyo
monto sea menor al saldo pendiente, sin rechazarlo por no cubrir el total.

#### Scenario: Pago que cubre el total del presupuesto
- **WHEN** se registra un único pago cuyo monto es igual al total del
  presupuesto
- **THEN** el sistema acepta el pago y el saldo pendiente queda en cero

#### Scenario: Pago parcial que no cubre el total
- **WHEN** se registra un pago cuyo monto es menor al saldo pendiente del
  presupuesto
- **THEN** el sistema acepta el pago sin exigir que cubra el resto del
  saldo

#### Scenario: Secuencia de pagos parciales que suman el total
- **WHEN** se registran varios pagos parciales sobre el mismo presupuesto
  cuya suma iguala su total
- **THEN** el sistema acepta cada pago individualmente y el saldo
  pendiente llega a cero tras el último

### Requirement: Determinar el estado de pago de un presupuesto
El sistema SHALL determinar el estado de pago de un presupuesto como "sin
pagos" cuando no tiene ningún pago registrado, "parcialmente abonado"
cuando tiene pagos registrados y su saldo pendiente es mayor a cero, y
"saldado" cuando su saldo pendiente es cero.

#### Scenario: Presupuesto sin pagos registrados
- **WHEN** se consulta el estado de pago de un presupuesto que no tiene
  ningún pago registrado
- **THEN** el sistema informa el estado "sin pagos"

#### Scenario: Presupuesto parcialmente abonado
- **WHEN** se consulta el estado de pago de un presupuesto que tiene al
  menos un pago registrado y un saldo pendiente mayor a cero
- **THEN** el sistema informa el estado "parcialmente abonado"

#### Scenario: Presupuesto saldado
- **WHEN** se consulta el estado de pago de un presupuesto cuyo saldo
  pendiente es cero
- **THEN** el sistema informa el estado "saldado"

### Requirement: Consultar el estado de pago de un presupuesto (CU47)
El sistema SHALL permitir consultar el estado de pago de un presupuesto de
forma independiente de la lista de sus pagos, para distinguir visualmente
uno parcialmente abonado de uno saldado.

#### Scenario: Consultar el estado de un presupuesto parcialmente abonado
- **WHEN** se consulta el estado de pago de un presupuesto parcialmente
  abonado
- **THEN** el sistema responde con el estado "parcialmente abonado"

#### Scenario: Consultar el estado de un presupuesto saldado
- **WHEN** se consulta el estado de pago de un presupuesto saldado
- **THEN** el sistema responde con el estado "saldado"

#### Scenario: Consultar el estado de un presupuesto inexistente
- **WHEN** se consulta el estado de pago de un presupuesto cuyo ID no
  existe
- **THEN** el sistema responde con un error de no encontrado
