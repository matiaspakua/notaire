# CU09 – Registrar deudas documentos de Cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU09 – Registrar deudas documentos de Cliente |
| **Actores** | Gestor/Escribano |
| **Propósito** | Verifica si en la documentación que entrega el cliente, existen deudas para cancelar, y permite registrar el pago de la misma. |
| **Descripción** | El Gestor/Escribano busca las gestiones de un cliente, indicando el nombre y apellido o tipo y número de identificación del mismo, luego selecciona una gestión en particular. El sistema presenta el detalle de la gestión y la lista de todos los documentos entregados por el cliente. Además se indica, para los que presentan deudas, el monto de la misma, y de los que registran pago, la fecha en que se realizó el mismo. Para los documentos que presentan y que aún no han sido registrados los datos de la misma, el Gestor/Escribano indica el monto y/o fecha de pago de los mismos y guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #19 (Informar seguimiento de documentos); CU19 |
| **GitHub ID** | #162 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano busca las gestiones de un cliente, indicando el nombre y apellido o tipo y número de identificación del mismo. |  |
| 2 |  | Busca y presenta la lista de gestiones del cliente indicado. |
| 3 | El Gestor/Escribano selecciona una gestión de la lista. |  |
| 4 |  | Presenta el detalle de la gestión y la lista de todos los documentos entregados por el cliente. Además se indica, para los que presentan deudas, el monto de la misma, y de los que registran pago, la fecha en que se realizó el mismo. Muestra por cada uno: (Nombre,; Si presenta deuda o no,; Monto de deuda,; Fecha de pago) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen gestiones asociadas al cliente indicado. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No existen documentos con pagos pendientes. | El sistema gestiona la excepción y notifica al usuario. |
