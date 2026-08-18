# CU16 – Archivar Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU16 – Archivar Gestión |
| **Actores** | Escribano/Gestor/Protocolista |
| **Propósito** | Finaliza una gestión. |
| **Descripción** | El Escribano/Gestor/Protocolista selecciona de una lista de gestiones en trámite disponibles, una de ellas y cambia el estado de la gestión a “archivada” e indica observaciones. El sistema genera un nuevo número de archivo indicando número de bibliorato y número de carpeta, para la misma. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #36 (Archivar trámites), RF #37 (Archivar trámite), RF #104 (Administrar carpetas de trámite), RF #106 (Estados de carpeta); CU19 |
| **GitHub ID** | #169 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Gestor/Protocolista solicita al sistema una lista de gestiones en trámite. |  |
| 2 |  | Busca y presenta la lista de gestiones indicada. |
| 3 | El Escribano/Gestor/Protocolista selecciona una gestión de la lista y la registra como “archivada” e indica algunas observaciones. |  |
| 4 |  | Registra los cambios realizados y genera un nuevo número de archivo indicando número de bibliorato y número de carpeta de archivo, para la misma. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La gestión ya tiene un número de archivo asociado. | El sistema gestiona la excepción y notifica al usuario. |
