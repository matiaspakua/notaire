# CU67 – Buscar Estados de Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU67 – Buscar Estados de Gestión |
| **Actores** | Escribano |
| **Propósito** | Busca la lista de estados de gestión registrados. |
| **Descripción** | El escribano solicita la lista de estados de gestión registrados. El sistema busca y devuelve una lista de todos los estados de gestión registrados. El escribano selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: el nombre del estado de gestión y las observaciones. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #59 (Ingresar nuevos estados), RF #107 (Estados y transiciones del trámite), RF #108 (Definir estados del trámite) |
| **GitHub ID** | #220 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano solicita la lista de estados de gestión. |  |
| 2 |  | Busca y devuelve una lista de todos los estados de gestión disponibles. |
| 3 | El escribano selecciona uno de los estados de gestión. |  |
| 4 |  | Muestra el detalle del estado de gestión seleccionado, donde se indica: (El nombre del estado de gestión.; Las observaciones.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen estados de gestión registrados. | El sistema gestiona la excepción y notifica al usuario. |
