# CU37 – Eliminar concepto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU37 – Eliminar concepto |
| **Actores** | Escribano |
| **Propósito** | Elimina un tipo de concepto. |
| **Descripción** | El Escribano decide eliminar un concepto. El sistema presenta una lista de todos los conceptos disponibles. El Escribano selecciona uno de ellos y confirma su eliminación. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #60 (Ingresar nuevos conceptos); CU66 |
| **GitHub ID** | #190 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide eliminar un concepto. |  |
| 2 |  | Presenta una lista de todos los conceptos disponibles. |
| 3 | El Escribano selecciona uno de ellos y confirma su eliminación. |  |
| 4 |  | Elimina el concepto indicado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen conceptos disponibles. | El sistema gestiona la excepción y notifica al usuario. |
