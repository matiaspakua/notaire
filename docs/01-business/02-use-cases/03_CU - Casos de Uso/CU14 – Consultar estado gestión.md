# CU14 – Consultar estado gestión (ELIMINADO *)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU14 – Consultar estado gestión (ELIMINADO *) |
| **Actores** | Cliente, Gestor/Escribano |
| **Propósito** | Permite informar el estado actual de una gestión. |
| **Descripción** | El Cliente se acerca a la escribanía para consultar el estado de su gestión. El Gestor/Escribano solicita el nombre, apellido o tipo y número de documento, para realizar la búsqueda. El sistema presenta una lista de gestiones asociadas al Cliente indicado. El Gestor/Escribano selecciona una gestión en particular. Se informa el estado actual y demás datos de la misma. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #24 (Consultar estado e historial de los trámites), RF #25 (Saber el estado de un trámite en un momento determinado) |
| **GitHub ID** | #167 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Cliente decide consultar el estado de su gestión. |  |
| 2 | El Gestor/Escribano solicita al Cliente el nombre, apellido o tipo y número de documento, para realizar la búsqueda. | Solicita nombre, apellido o tipo y número de documento. |
| 3 |  | Busca las gestiones asociadas al cliente indicado y muestra una lista de las mismas. |
| 4 | El Gestor/Escribano selecciona una gestión en particular. |  |
| 5 |  | Muestra: (Estado actual,; Número de gestión,; Encabezado,; Fecha de inicio,; Escribano a cargo,; Número de bibliorato,; Trámites asociados,; Escrituras asociadas si corresponde,; Observaciones,; Nombre del/los clientes de la gestión seleccionada.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Excepción operativa | No existen gestiones asociadas al Cliente indicado. |
