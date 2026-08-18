# CU38 – Eliminar tipo de documento

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU38 – Eliminar tipo de documento |
| **Actores** | Escribano |
| **Propósito** | Elimina un tipo de documento. |
| **Descripción** | El Escribano decide eliminar un tipo de documento. El sistema presenta una lista de todos los tipos de documento disponibles. El Escribano selecciona uno de ellos y confirma su eliminación. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #57 (Ingresar nuevos documentos); CU65 |
| **GitHub ID** | #191 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide eliminar un tipo de documento en particular. |  |
| 2 |  | Muestra una lista de todos los tipos de documento disponibles. |
| 3 | Selecciona un tipo de documento y confirma su eliminación. |  |
| 4 |  | Elimina el documento seleccionado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen documentos disponibles. | El sistema gestiona la excepción y notifica al usuario. |
