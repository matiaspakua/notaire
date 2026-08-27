# CU11 – Ingresar para inscripción

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU11 – Ingresar para inscripción |
| **Actores** | Gestor/Escribano |
| **Propósito** | Registrar el ingreso, para la inscripción, de un testimonio perteneciente a una escritura. |
| **Descripción** | El Gestor/Escribano selecciona una escritura, de una lista de escrituras. El sistema muestra los datos de la Escritura, su Testimonio para Inscribir y sus respectivas nomenclaturas catastrales en caso de tratarse de un inmueble. El Gestor/Escribano indica los datos para ingresar el Testimonio a inscribir. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #34 (Registrar testimonios inscriptos), RF #118 (Seguimiento de presentación para inscripción); CU62 |
| **GitHub ID** | #164 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano decide Registrar la inscripción, de un testimonio, de una escritura. |  |
| 2 |  | El sistema muestra una lista de escrituras. |
| 3 | El Gestor/Escribano selecciona un escritura, para registrar un inscripción. |  |
| 4 |  | Muestra: (Número de escritura; Fecha; Folios utilizados; Nomenclaturas catastrales asociadas (si corresponde)) Y solicita: (Fecha de ingreso; Observaciones) |
| 5 | El Gestor/Escribano registra: fecha en que fue ingresado para inscribir, junto con algunas observaciones. El número de cartón se registra al momento del retiro (ver CU12). |  |
| 6 |  | Registra lo datos ingresados. |
| 7 | El testimonio es llevado a inscribir. |  |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen escrituras listas para inscribir. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | El testimonio seleccionado aún no fue verificado (ver CU08). | El sistema gestiona la excepción y notifica al usuario. |
| 5.2 | El testimonio ya tiene un movimiento de inscripción abierto (sin retirar). | El sistema gestiona la excepción y notifica al usuario. |
