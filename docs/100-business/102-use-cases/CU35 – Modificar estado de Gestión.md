# CU35 – Modificar estado de Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU35 – Modificar estado de Gestión |
| **Actores** | Escribano |
| **Propósito** | Modifica el nombre de un estado. |
| **Descripción** | El Escribano decide modificar los atributos de un estado de gestión. El sistema presenta una lista de todos los estados disponibles y el Escribano selecciona uno se ellos. El sistema muestra el nombre y las observaciones del estado seleccionado. El Escribano modifica algunos de los atributos y guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #59 (Ingresar nuevos estados), RF #107 (Estados y transiciones del trámite), RF #108 (Definir estados del trámite), RF #109 (Definir transiciones válidas de estado); CU67 |
| **GitHub ID** | #188 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar el nombre de un estado de gestión. |  |
| 2 |  | Muestra una lista de todos los estados disponibles. |
| 3 | El Escribano selecciona un estado de la lista. |  |
| 4 |  | Muestra el nombre y las observaciones del estado. |
| 5 | El Escribano modifica el estado u observaciones, y confirma los cambios. |  |
| 6 |  | Registra la modificación. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen estados de gestión registrados. | El sistema gestiona la excepción y notifica al usuario. |
| 6.1 | Alguno de los datos ingresados no es válido. | El sistema gestiona la excepción y notifica al usuario. |
