# CU57 – Eliminar tipo de trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU57 – Eliminar tipo de trámite |
| **Actores** | Escribano |
| **Propósito** | Elimina un tipo de trámite (deshabilita) |
| **Descripción** | El Escribano decide eliminar un tipo de trámite. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona uno de ellos y confirma su eliminación. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #56 (Ingresar nuevos trámites); CU64 |
| **GitHub ID** | #210 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide eliminar un tipo de trámite en particular. |  |
| 2 |  | Muestra una lista de todos los tipos de trámite disponibles. |
| 3 | El Escribano selecciona un tipo de trámite. |  |
| 4 |  | Busca y muestra la información del tipo de trámite seleccionado. |
| 5 | El Escribano confirma la eliminación del tipo de trámite. |  |
| 6 |  | Elimina el tipo de trámite seleccionado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de trámite disponibles. | El sistema gestiona la excepción y notifica al usuario. |
