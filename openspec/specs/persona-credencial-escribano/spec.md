# persona-credencial-escribano Specification

## Purpose

Da a la pantalla de personas un lugar dedicado para dar de alta o modificar
la credencial de escribano de una persona (`registroEscribano`), que ya
existe en el modelo de datos pero no es gestionable desde la UI (CU48,
CU51).

## Requirements

### Requirement: Dar de alta el registro de escribano desde personas
El sistema SHALL permitir ingresar el número de registro de escribano de una
persona existente desde la pantalla de personas, según CU48.

#### Scenario: Alta de registro de escribano
- **WHEN** un usuario busca una persona existente sin número de registro de
  escribano y le asigna uno
- **THEN** el sistema guarda el número de registro de escribano en esa
  persona

### Requirement: Modificar el registro de escribano desde personas
El sistema SHALL permitir modificar el número de registro de escribano de
una persona que ya lo tiene, desde la pantalla de personas, según CU51.

#### Scenario: Modificación de registro de escribano
- **WHEN** un usuario busca una persona que ya tiene un número de registro
  de escribano y lo modifica
- **THEN** el sistema guarda el nuevo número de registro de escribano en esa
  persona
