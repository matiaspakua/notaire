# CU39 – Crear Plantilla Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU39 – Crear Plantilla Presupuesto |
| **Actores** | Escribano |
| **Propósito** | Crea la plantilla de un presupuesto |
| **Descripción** | El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite, y el sistema muestra la lista de conceptos disponibles. El Escribano indica los conceptos que se deben abonar para el mismo. Luego guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #6 (Editar plantillas de presupuestos), RF #61 (Administrar plantillas), RF #66 (Plantillas de presupuestos), RF #67 (Crear nuevas plantillas de presupuestos); CU64, CU66 |
| **GitHub ID** | #192 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular. |  |
| 2 |  | Presenta una lista de todos los tipos de trámite disponibles. |
| 3 | El Escribano selecciona un tipo de trámite de la lista. |  |
| 4 |  | Muestra la lista de conceptos disponibles para ese trámite. |
| 5 | El Escribano selecciona los conceptos asociados al mismo, y luego confirma la nueva plantilla de presupuesto. |  |
| 6 |  | Registra una nueva plantilla de presupuesto. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | 1. No existen tipos de trámite disponibles. | El sistema gestiona la excepción y notifica al usuario. |
| 2.1 | 2. Los tipos de trámite existentes ya tiene una plantilla de presupuesto asociada. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No existen conceptos registrados. | El sistema gestiona la excepción y notifica al usuario. |
