# descuentos-recargos-presupuesto Specification

## Purpose
Permite clasificar un ítem de presupuesto como descuento o recargo con un
motivo obligatorio, y refleja ese signo en el total del presupuesto y en un
reporte consultable (CU45, CU71).
## Requirements
### Requirement: Clasificar un ítem de presupuesto por tipo
El sistema SHALL permitir clasificar un ítem de presupuesto como normal,
descuento o recargo. Un ítem sin tipo explícito SHALL comportarse como
normal.

#### Scenario: Crear un ítem normal sin tipo explícito
- **WHEN** se crea un ítem de presupuesto sin especificar tipo
- **THEN** el sistema lo trata como tipo "normal"

#### Scenario: Crear un ítem de tipo descuento
- **WHEN** se crea un ítem de presupuesto con tipo "descuento" y un motivo
  válido
- **THEN** el sistema acepta el ítem clasificado como descuento

#### Scenario: Crear un ítem de tipo recargo
- **WHEN** se crea un ítem de presupuesto con tipo "recargo" y un motivo
  válido
- **THEN** el sistema acepta el ítem clasificado como recargo

### Requirement: Exigir motivo estructurado en descuentos y recargos
El sistema SHALL rechazar la creación o actualización de un ítem de tipo
descuento o recargo cuando no incluye un motivo no vacío.

#### Scenario: Rechazar un descuento sin motivo
- **WHEN** se intenta crear un ítem de tipo "descuento" sin motivo
- **THEN** el sistema rechaza la operación con un error de validación

#### Scenario: Rechazar un recargo sin motivo
- **WHEN** se intenta crear un ítem de tipo "recargo" sin motivo
- **THEN** el sistema rechaza la operación con un error de validación

#### Scenario: Aceptar un ítem normal sin motivo
- **WHEN** se crea un ítem de tipo "normal" sin motivo
- **THEN** el sistema acepta el ítem sin exigir motivo

### Requirement: Calcular el total del presupuesto según el tipo de ítem
El sistema SHALL calcular el total de un presupuesto sumando los ítems de
tipo normal y recargo, y restando los ítems de tipo descuento.

#### Scenario: Total con un ítem de descuento
- **WHEN** un presupuesto tiene ítems normales y un ítem de tipo descuento
- **THEN** el total del presupuesto resta el valor del ítem de descuento

#### Scenario: Total con un ítem de recargo
- **WHEN** un presupuesto tiene ítems normales y un ítem de tipo recargo
- **THEN** el total del presupuesto suma el valor del ítem de recargo

#### Scenario: Total sin descuentos ni recargos
- **WHEN** un presupuesto solo tiene ítems de tipo normal
- **THEN** el total del presupuesto se calcula sumando todos los ítems,
  igual que hoy

### Requirement: Consultar los descuentos y recargos de un presupuesto
El sistema SHALL permitir consultar, para un presupuesto dado, la lista de
sus ítems de tipo descuento y recargo junto con su motivo.

#### Scenario: Consultar descuentos y recargos de un presupuesto con ambos
- **WHEN** se consulta el reporte de descuentos y recargos de un
  presupuesto que tiene al menos un ítem de cada tipo
- **THEN** el sistema responde con la lista de esos ítems y su motivo

#### Scenario: Consultar descuentos y recargos de un presupuesto sin ninguno
- **WHEN** se consulta el reporte de descuentos y recargos de un
  presupuesto que solo tiene ítems normales
- **THEN** el sistema responde con una lista vacía

#### Scenario: Consultar descuentos y recargos de un presupuesto inexistente
- **WHEN** se consulta el reporte de descuentos y recargos de un
  presupuesto cuyo ID no existe
- **THEN** el sistema responde con un error de no encontrado

