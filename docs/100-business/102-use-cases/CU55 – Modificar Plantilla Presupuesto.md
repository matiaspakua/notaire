# CU55 – Modificar Plantilla Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU55 – Modificar Plantilla Presupuesto |
| **Actores** | Escribano |
| **Propósito** | Modificar plantilla de presupuesto |
| **Descripción** | El Escribano decide modificar una plantilla de presupuesto, para un tipo trámite en particular. El sistema presenta una lista de los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite, y el sistema muestra la lista de conceptos asociados a ese tipo de trámite en la plantilla. El Escribano modifica los conceptos que se deben abonar para el mismo. Luego guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #6 (Editar plantillas de presupuestos), RF #61 (Administrar plantillas), RF #66 (Plantillas de presupuestos), RF #68 (Modificar plantillas de presupuestos); CU64, CU66 |
| **GitHub ID** | #208 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar una plantilla de presupuesto, para un tipo de trámite en particular. |  |
| 2 |  | Presenta una lista de todos los tipos de trámite disponibles. |
| 3 | El Escribano selecciona un tipo de trámite de la lista. |  |
| 4 |  | Muestra la lista de conceptos disponibles para el tipo de trámite. |
| 5 | El Escribano modifica los conceptos que se deben abonar para el mismo, confirmando los datos ingresados. |  |
| 6 |  | Registra la modificación de la plantilla de presupuesto. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de trámite con plantillas de presupuesto asociadas. | El sistema gestiona la excepción y notifica al usuario. |
| 2.2 | No existen conceptos disponibles. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No existen conceptos registrados. | El sistema gestiona la excepción y notifica al usuario. |
