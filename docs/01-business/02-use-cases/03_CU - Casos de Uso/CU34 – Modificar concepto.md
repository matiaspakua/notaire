# CU34 – Modificar concepto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU34 – Modificar concepto |
| **Actores** | Escribano |
| **Propósito** | Modifica los datos de un concepto. |
| **Descripción** | El Escribano decide modificar los datos de un concepto. El sistema presenta la lista de conceptos disponibles y el escribano selecciona uno de ellos. El sistema presenta los datos del mismo. El escribano modifica alguno de los datos indicados y guarda los cambios. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #60 (Ingresar nuevos conceptos); CU66 |
| **GitHub ID** | #187 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar los datos de un concepto. |  |
| 2 |  | Presenta la lista de conceptos disponibles. |
| 3 | El Escribano selecciona uno de ellos. |  |
| 4 |  | Presenta: (Nombre del concepto; Valor; Porcentaje; Si es valor fijo o variable) |
| 5 | El escribano modifica alguno de los datos indicados. |  |
| 6 |  | Guarda el concepto modificado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen conceptos disponibles. | El sistema gestiona la excepción y notifica al usuario. |
