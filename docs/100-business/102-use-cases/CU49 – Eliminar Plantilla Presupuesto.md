# CU49 – Eliminar Plantilla Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU49 – Eliminar Plantilla Presupuesto |
| **Actores** | Escribano |
| **Propósito** | Eliminar plantilla de presupuesto |
| **Descripción** | El Escribano decide eliminar una plantilla de presupuesto, de un tipo de trámite en particular. El sistema presenta una lista de los tipos de trámite disponibles que tienen plantillas de presupuesto asociadas. El Escribano selecciona un tipo de trámite, y confirma que quiere eliminar la plantilla de presupuesto del mismo. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #6 (Editar plantillas de presupuestos), RF #61 (Administrar plantillas), RF #66 (Plantillas de presupuestos), RF #69 (Eliminar plantillas de presupuestos); CU64 |
| **GitHub ID** | #202 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide eliminar una plantilla de presupuesto. |  |
| 2 |  | Presenta una lista de los tipos de trámite disponibles que tienen plantillas de presupuesto asociadas. |
| 3 | El Escribano selecciona un tipo de trámite. |  |
| 4 |  | Solicita una confirmación para eliminar la plantilla de presupuesto del mismo. |
| 5 | Confirma que quiere eliminarla. |  |
| 6 |  | Registra eliminación. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de trámite con plantillas de presupuesto asociadas. | El sistema gestiona la excepción y notifica al usuario. |
