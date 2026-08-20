# CU15 – Procesar pago

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU15 – Procesar pago |
| **Actores** | Cliente, Recepcionista/Escribano |
| **Propósito** | Registra el pago de un trámite de una gestión. |
| **Descripción** | Un Cliente se acerca a la escribanía para abonar una gestión. El Recepcionista/Escribano solicita los datos necesarios para realizar la búsqueda. El sistema presenta los presupuestos pendientes asociados al Cliente. El Recepcionista/Escribano selecciona un presupuesto de la lista, y se muestra la información correspondiente. El Cliente indica el monto que desea abonar, y el Recepcionista/Escribano genera un nuevo pago registrando el monto del mismo, fecha de pago, método de pago y observaciones adicionales. El método de pago queda persistido junto con el pago y se puede consultar posteriormente. Finalmente se genera el recibo común correspondiente. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #20 (Abonar trámite), RF #21 (Registrar quién abona el trámite), RF #22 (Abonar presupuestos en cuotas), RF #23 (Generar e imprimir recibos de pagos); CU19, CU60 |
| **GitHub ID** | #168 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Cliente de acerca a la escribanía y solicita abonar un trámite de una gestión. |  |
| 2 | El Recepcionista/Escribano solicita el número de presupuesto, o tipo y número de identificación, o nombre y apellido del cliente, para realizar una búsqueda. |  |
| 3 |  | Busca y presenta, el presupuesto encontrado, o la lista de presupuestos pendientes asociados a una gestión o al cliente indicado. |
| 4 | El Recepcionista/Escribano selecciona un presupuesto de la lista. |  |
| 5 |  | Muestra los datos del presupuesto seleccionado: (Número de gestión a la que pertenece; Encabezado de gestión; Número de presupuesto; Trámite asociado; Ítems con los valores, porcentajes y observaciones correspondientes; Total) |
| 6 | El Recepcionista/Escribano solicita al Cliente que le indique el monto del pago a realizar. |  |
| 7 | El Cliente indica el monto que desea abonar. |  |
| 8 | El Recepcionista/Escribano solicita generar un nuevo pago. |  |
| 9 |  | Solicita el monto del mismo, fecha de pago, método de pago y observaciones adicionales. |
| 10 | El Recepcionista/Escribano indica los datos solicitados y guarda los mismos. |  |
| 11 |  | Registra el pago realizado (incluyendo el método de pago), calcula el saldo pendiente y lo muestra. |
| 12 | Solicita la generación del recibo correspondiente. |  |
| 13 |  | Genera el recibo común correspondiente, donde figura: (Fecha de Pago; Número de presupuesto; Número de pago; Nombre, Apellido, Tipo y número de identificación del Cliente; Número de la gestión asociada.) |
| 14 | El Recepcionista/Escribano hace entrega del recibo generado al Cliente. |  |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 3.1 | 2. El Recepcionista/Escribano informa al Cliente que no tiene deudas pendientes. | 1. El Cliente indicado no presenta presupuestos pendientes. |
| 3.2 | 1. El Cliente no tiene presupuestos asociados. | El sistema gestiona la excepción y notifica al usuario. |
