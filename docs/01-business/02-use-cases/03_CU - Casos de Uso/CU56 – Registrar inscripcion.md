# CU56 – Registrar inscripción

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU56 – Registrar inscripción |
| **Actores** | Gestor/Escribano |
| **Propósito** | Registrar la inscripción, de un testimonio, perteneciente a una escritura. |
| **Descripción** | Luego de que es devuelto el testimonio ya inscripto, se registra: fecha de inscripción y el número de matrícula con el que fue inscripto y observaciones, donde finalmente queda habilitado para ser retirado por el cliente. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #34 (Registrar testimonios inscriptos), RF #118 (Seguimiento de presentación para inscripción); CU62 |
| **GitHub ID** | #209 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano decide registrar la inscripción de un testimonio, para una escritura. |  |
| 2 |  | El sistema muestra una lista de escrituras que poseen testimonio para inscribir, y sus respectivas nomenclaturas catastrales en caso de tratarse de un inmueble. |
| 3 | El Gestor/Escribano selecciona un escritura, para registrar un inscripción. |  |
| 4 |  | Muestra: (Número de escritura; Fecha; Folios utilizados; Nomenclaturas catastrales asociadas (si corresponde); Número de cartón; Fecha de ingreso) Y solicita: (Fecha de salida; Fecha de inscripción; Número de Matrícula; Observaciones) Para registrar la inscripción. |
| 5 | El Escribano, ingresa los datos solicitados, y confirma la inscripción. |  |
| 6 |  | Registra los datos ingresados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La escritura seleccionada no tiene testimonios para inscripción. | El sistema gestiona la excepción y notifica al usuario. |
