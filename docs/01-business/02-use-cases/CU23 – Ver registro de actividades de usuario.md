# CU23 – Ver registro de actividades de Usuario

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU23 – Ver registro de actividades de Usuario |
| **Actores** | Escribano |
| **Propósito** | Permite visualizar el historial de actividades de un Usuario. |
| **Descripción** | El Escribano selecciona un Usuario de una lista de Usuarios disponibles. El sistema solicita que se indique un intervalo de fechas para auditar. El Escribano ingresa los datos requeridos, y el sistema presenta la lista de actividades llevadas a cabo por el Usuario. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #46 (Registro de auditoría); CU61 |
| **GitHub ID** | #176 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide ver el historial de actividades de un usuario. |  |
| 2 |  | Solicita nombre y apellido o tipo y número de identificación de la persona. |
| 3 | Ingresa el nombre y apellido o tipo y número de identificación de la persona. |  |
| 4 |  | Busca la persona asociada a los datos ingresados. |
| 5 | Confirma, que es la persona indicada. |  |
| 6 |  | Muestra una lista, con las actividades realizadas por el usuario indicando: (Fecha; Módulo; Nombre de usuario; Cambios realizados) con la posibilidad de imprimir un reporte. |
| 7 | Imprime un reporte del historial de actividades del usuario. |  |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 6.1 | El usuario no tiene un historial de movimientos. | El sistema gestiona la excepción y notifica al usuario. |
