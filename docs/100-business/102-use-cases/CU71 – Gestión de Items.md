# CU71 – Gestión de Ítems

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU71 – Gestión de Ítems |
| **Actores** | Escribano, Administrador |
| **Propósito** | Permite gestionar los ítems (conceptos de gasto) que componen un presupuesto. |
| **Descripción** | El sistema permite definir los ítems individuales que pueden ser agregados a un presupuesto, como sellados, honorarios, tasas registrales, etc. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #9 (Agregar ítems adicionales a los presupuestos); CU01, CU45 |
| **GitHub ID** | #300 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Administrador selecciona la opción de gestión de ítems. |  |
| 2 |  | El sistema muestra la lista de ítems existentes. |
| 3 | El Administrador ingresa el nombre, descripción y valor base del ítem. |  |
| 4 |  | El sistema valida los datos. |
| 5 | El Administrador confirma el registro. |  |
| 6 |  | El sistema guarda el ítem y lo habilita para su uso en presupuestos. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
| 3a | El Administrador clasifica el ítem como Descuento o Recargo | El sistema exige un motivo obligatorio antes de permitir el guardado (issue #822). |

## Tipo de Ítem (Normal / Descuento / Recargo)

Todo ítem tiene un `tipo`: `NORMAL` (por defecto), `DESCUENTO` o `RECARGO`. Al seleccionar `DESCUENTO` o `RECARGO` en el formulario, el campo `motivo` se vuelve obligatorio y se valida tanto en el cliente como en el servidor. El monto de un ítem `DESCUENTO` resta del total del presupuesto; el de un `RECARGO` suma (ver CU45).

## Agregar copias de ítems del catálogo a un presupuesto (Issue #834)

Desde el diálogo de ítems de un presupuesto (`/dashboard/presupuestos`),
el Escribano puede elegir un ítem existente del catálogo (`Item` sin
`fk_id_presupuesto`) y agregarlo como copia al presupuesto (endpoint
`POST /api/v1/presupuestos/{id}/items-desde-catalogo`, recibe una lista
de IDs de ítems del catálogo). Cada copia es un `Item` nuevo e
independiente asociado al presupuesto; modificarla no altera el ítem del
catálogo original. Ver también CU39 – Crear Plantilla Presupuesto para la
carga masiva de ítems desde la plantilla del tipo de trámite.
