<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation (CU15 – Procesar pago) remains their source of
     truth - cite it, do not duplicate it. -->

## Purpose

Permite emitir, en formato PDF, el comprobante de pago que CU15 exige entregar
al cliente al cierre de una gestión de cobranza.

## ADDED Requirements

### Requirement: Emitir recibo de un pago existente
El sistema SHALL generar un PDF de recibo para un pago ya persistido,
detallando el cliente que abona, la fecha de pago, el/los concepto(s)
abonado(s) y el total abonado.

#### Scenario: Recibo de un pago simple
- **WHEN** se solicita el recibo de un `idPago` existente cuyo presupuesto
  tiene un cliente e ítems asociados
- **THEN** el sistema responde `200 OK` con `Content-Type: application/pdf`
  cuyo contenido incluye el nombre del cliente, la fecha del pago, el nombre
  de al menos un ítem del presupuesto y el monto del pago

#### Scenario: Recibo de un pago parcial o en cuotas
- **WHEN** se solicita el recibo de un pago que corresponde a una cuota
  parcial de un presupuesto con saldo pendiente
- **THEN** el recibo muestra el monto de esa cuota (el `monto` del pago),
  no el total del presupuesto

#### Scenario: Recibo de un pago inexistente
- **WHEN** se solicita el recibo de un `idPago` que no existe
- **THEN** el sistema responde `404 Not Found`
