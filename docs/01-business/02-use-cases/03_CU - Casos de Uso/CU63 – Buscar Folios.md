# CU63 – Buscar Folios

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU63 – Buscar Folios |
| **Actores** | Escribano |
| **Propósito** | Busca folios |
| **Descripción** | El escribano requiere utilizar o modificar un folio o un conjunto de folios, para lo cual ingresa el número de registro de escribano y el año. El sistema busca y devuelve una lista de todos los folios asociados al registro de escribano indicado. El escribano selecciona uno o varios de los folios listados y el sistema presenta la siguiente información de cada uno: el número de folios, el año y el estado actual del mismo. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #58 (Ingresar nuevos folios), RF #94 (Administrar folios), RF #95 (Cargar folios del Colegio Notarial), RF #96 (Control de numeración correlativa de folios), RF #99 (Seguimiento de disponibilidad de folios) |
| **GitHub ID** | #216 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano requiere utilizar o modificar un folio o un conjunto de folios |  |
| 2 |  | Solicita que se indique el número de registro de escribano y el año. |
| 3 | El escribano ingresa los datos solicitados. |  |
| 4 |  | Busca y devuelve la lista de folios correspondientes al registro de escribano y el año indicados. |
| 5 | El escribano selecciona uno o varios de los folios listados. |  |
| 6 |  | Por cada folio, indica: (el número de folios; el año; el estado actual del mismo.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen folios registrados para el registro y año indicados. | El sistema gestiona la excepción y notifica al usuario. |
