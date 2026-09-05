# Numeración de Escrituras Specification

## Purpose

Controla que el número de escritura asignado al preparar o firmar una
escritura sea único y correlativo dentro del protocolo, año y escribano
correspondientes, sin faltantes no justificados ni duplicados (CU86).

## Requirements

### Requirement: Validar correlatividad al guardar una escritura
El sistema SHALL calcular el siguiente número correlativo esperado dentro
del protocolo (principal o auxiliar), año y escribano de la escritura, y
comparar el número propuesto contra ese correlativo antes de guardar.

#### Scenario: Número coincide con el correlativo esperado
- **WHEN** se guarda una escritura cuyo número coincide con el correlativo
  esperado para su protocolo, año y escribano
- **THEN** el sistema guarda la escritura con normalidad

### Requirement: Rechazar números duplicados
El sistema SHALL rechazar el guardado de una escritura cuyo número ya fue
usado por otra escritura del mismo protocolo, año y escribano.

#### Scenario: Número ya utilizado
- **WHEN** se intenta guardar una escritura con un número ya usado por otra
  escritura del mismo protocolo, año y escribano
- **THEN** el sistema rechaza el guardado e informa el número duplicado

### Requirement: Exigir justificación ante un salto de numeración
El sistema SHALL exigir una justificación registrada cuando el número
propuesto deja un salto respecto al correlativo esperado, y SHALL guardar
esa justificación junto a la escritura.

#### Scenario: Salto sin justificación
- **WHEN** se intenta guardar una escritura cuyo número deja un salto
  respecto al correlativo esperado, sin indicar justificación
- **THEN** el sistema rechaza el guardado y solicita la justificación del
  salto

#### Scenario: Salto con justificación
- **WHEN** se guarda una escritura cuyo número deja un salto respecto al
  correlativo esperado, indicando una justificación (por ejemplo,
  escritura anulada o "no pasó")
- **THEN** el sistema guarda la escritura junto con la justificación
  registrada

### Requirement: Numeración del Protocolo Auxiliar independiente del Principal
El sistema SHALL controlar la correlatividad del Protocolo Auxiliar en
forma independiente de la del Protocolo Principal.

#### Scenario: Escritura de Protocolo Auxiliar no afecta la numeración del Principal
- **WHEN** se guarda una escritura sobre un folio de Protocolo Auxiliar
- **THEN** el sistema calcula y valida su correlativo únicamente contra
  otras escrituras de Protocolo Auxiliar del mismo año y escribano, sin
  considerar la numeración del Protocolo Principal
