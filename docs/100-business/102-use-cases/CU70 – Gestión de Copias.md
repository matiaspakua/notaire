# CU70 – Gestión de Copias

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU70 – Gestión de Copias |
| **Actores** | Escribano, Gestor |
| **Propósito** | Permite gestionar las copias (testimonios) de las escrituras matrices. |
| **Descripción** | El sistema registra cada copia emitida de una escritura, indicando quién la solicitó, la fecha de emisión y el tipo de copia (primer testimonio, segundo testimonio, etc.). |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #32 (Administrar inscripciones), RF #33 (Generar y registrar testimonios de escrituras), RF #120 (Impresión de testimonios); CU05, CU07 |
| **GitHub ID** | #243 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano selecciona una escritura. |  |
| 2 |  | El sistema muestra el detalle de la escritura y sus copias anteriores. |
| 3 | El Escribano indica la generación de una nueva copia. |  |
| 4 |  | El sistema solicita el tipo de copia y el solicitante. |
| 5 | El Escribano confirma la emisión. |  |
| 6 |  | El sistema registra la copia y permite su impresión. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La escritura no tiene un protocolo asociado aún. | El sistema gestiona la excepción y notifica al usuario. |
