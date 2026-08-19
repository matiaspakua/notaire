# CU73 – Registro de Auditoría

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU73 – Registro de Auditoría |
| **Actores** | Administrador |
| **Propósito** | Permite consultar el historial de acciones realizadas por los usuarios en el sistema. |
| **Descripción** | El sistema registra automáticamente todas las operaciones sensibles (creación, modificación, eliminación) y permite al administrador filtrar y visualizar quién realizó qué acción y cuándo. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #46 (Registro de auditoría); CU23 |
| **GitHub ID** | #309 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Administrador selecciona la opción de Auditoría. |  |
| 2 |  | El sistema muestra la lista general de eventos de auditoría. |
| 3 | El Administrador aplica filtros (usuario, fecha, tipo de operación). |  |
| 4 |  | El sistema filtra la lista y la presenta al administrador. |
| 5 | El Administrador visualiza el detalle de una operación. |  |
| 6 |  | El sistema muestra los datos técnicos del cambio (valor anterior, valor nuevo). |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
