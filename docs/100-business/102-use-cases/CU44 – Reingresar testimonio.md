# CU44 – Reingresar testimonio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU44 – Reingresar testimonio |
| **Actores** | Gestor/Escribano |
| **Propósito** | Permite registrar el reingreso de un testimonio. |
| **Descripción** | Un testimonio ha sido devuelto observado. El Gestor/Escribano solicita el reingreso del mismo, seleccionando una escritura de una lista de escrituras disponibles. El sistema muestra el testimonio a reingresar, asociado a la escritura seleccionada. El Gestor/Escribano ingresa los datos correspondientes, y el sistema registra el reingreso del mismo. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #34 (Registrar testimonios inscriptos), RF #118 (Seguimiento de presentación para inscripción); CU62 |
| **GitHub ID** | #197 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un testimonio ha sido devuelto observado. |  |
| 2 | El Gestor/Escribano decide registrar el reingreso de un testimonio. |  |
| 3 |  | Muestra una lista de disponibles. |
| 4 | El Gestor/Escribano selecciona una escritura. |  |
| 5 |  | Muestra: (Número de escritura; Fecha; Folios utilizados; Nomenclaturas catastrales asociadas (si corresponde)) Y solicita: (Fecha salida; Fecha de reingreso; Número de cartón; Si fue observado o no; Observaciones) |
| 6 | El Gestor/Escribano indica los datos solicitados, y confirma su reingreso. |  |
| 7 |  | Registra el reingreso del testimonio. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 5.1 | El testimonio seleccionado no fue retirado previamente (ver CU12). | El sistema gestiona la excepción y notifica al usuario. |
