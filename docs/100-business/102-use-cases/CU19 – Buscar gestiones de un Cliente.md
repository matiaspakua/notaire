# CU19 – Buscar gestiones de un Cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU19 – Buscar gestiones de un Cliente |
| **Actores** | Escribano, Recepcionista/Gestor, Cliente. |
| **Propósito** | Presenta una lista de las gestiones realizadas por un Cliente. |
| **Descripción** | El Escribano/Recepcionista busca todas las gestiones asociadas a un Cliente, indicando al sistema nombre, apellido o tipo y número de identificación del mismo. El sistema muestra las gestiones encontradas. El Escribano/Recepcionista selecciona una gestión en particular, y el sistema muestra el detalle de la misma. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #38 (Administrar clientes), RF #42 (Buscar gestiones de cliente); CU61 |
| **GitHub ID** | #172 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista solicita una gestión asociada a un cliente en particular. |  |
| 2 |  | Solicita que se indique el cliente por: nombre, apellido o tipo y número de identificación del mismo. |
| 3 | El Escribano/Recepcionista indica los datos solicitados. |  |
| 4 |  | El sistema muestra las gestiones encontradas asociadas al cliente, indicando: (Número de gestión; Encabezado; Fecha de inicio; Estado; Número de bibliorato; Observaciones.) |
| 5 | El Escribano/Recepcionista selecciona una gestión en particular. |  |
| 6 |  | El sistema muestra el detalle de la gestión, indicando: (Número de gestión; Encabezado; Fecha de inicio; Escribano a cargo; Estado actual; Número de bibliorato; Trámites asociados; Clientes involucrados; Escrituras asociadas; Observaciones.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen gestiones asociadas al Cliente. | El sistema gestiona la excepción y notifica al usuario. |
