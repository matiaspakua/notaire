# persona-validacion-duplicados Specification

## Purpose
Prevents two `Persona` records from coexisting with the same
`TipoIdentificacion` and `numeroIdentificacion`, so presupuestos,
gestiones and pagos are never split across two unlinked records of the
same real person (CU17, CU18).
## Requirements
### Requirement: Rechazar alta de persona con documento duplicado
El sistema SHALL rechazar la creación de una `Persona` cuando ya existe
otra `Persona` con el mismo `TipoIdentificacion` y
`numeroIdentificacion`, según CU17 y CU18.

#### Scenario: Alta exitosa con documento no registrado
- **WHEN** un usuario crea una persona con un tipo y número de
  identificación que no pertenece a ninguna persona existente
- **THEN** el sistema crea la persona

#### Scenario: Rechazo de alta con documento ya registrado
- **WHEN** un usuario intenta crear una persona con el mismo tipo y
  número de identificación que una persona ya existente
- **THEN** el sistema rechaza la creación y responde con un error que
  identifica a la persona existente

### Requirement: Rechazar edición de persona hacia un documento duplicado
El sistema SHALL rechazar la edición de una `Persona` cuando el tipo y
número de identificación propuestos ya pertenecen a otra `Persona`
distinta, según CU17 y CU18.

#### Scenario: Edición exitosa sin cambiar el documento
- **WHEN** un usuario edita una persona existente sin cambiar su tipo ni
  número de identificación
- **THEN** el sistema guarda los cambios

#### Scenario: Rechazo de edición hacia un documento de otra persona
- **WHEN** un usuario edita una persona cambiando su tipo o número de
  identificación al de otra persona ya existente
- **THEN** el sistema rechaza la edición y responde con un error que
  identifica a la persona existente con ese documento

