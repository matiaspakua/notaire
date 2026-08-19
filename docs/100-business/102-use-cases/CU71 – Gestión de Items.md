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
