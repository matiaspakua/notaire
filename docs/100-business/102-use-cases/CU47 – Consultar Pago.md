# CU47- Consultar pago

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU47- Consultar pago |
| **Actores** | Recepcionista/Escribano |
| **Propósito** | Consulta los pagos realizados, por gestión. |
| **Descripción** | El Recepcionista/Escribano decide consultar los pagos realizados, hasta el momento, en base a una gestión en particular. El Recepcionista/Escribano busca la gestión por número de presupuesto, o por nombre y apellido, o tipo y número de identificación. El sistema presenta la lista de presupuestos asociados al cliente buscado. El Recepcionista/Escribano selecciona un presupuesto en particular, y el sistema detalla el mismo. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #20 (Abonar trámite), RF #22 (Abonar presupuestos en cuotas); CU19, CU60; `GET /api/v1/presupuestos/{id}/resumen` (#820) |
| **GitHub ID** | #200 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Recepcionista/Escribano decide consultar los pagos realizados por un cliente. |  |
| 2 |  | El sistema solicita, número de presupuesto, o nombre y apellido, o tipo y número de identificación del cliente, para la búsqueda. |
| 3 | El Recepcionista/Escribano ingresa los datos solicitados. |  |
| 4 |  | Presenta la lista de presupuestos asociados al cliente. |
| 5 | El Recepcionista/Escribano selecciona un presupuesto. |  |
| 6 |  | Presenta: (Número de gestión; Encabezado; Número de presupuesto; Total; Saldo; Número de pago; Monto de pago; Fecha de pago; Observaciones) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen presupuestos asociados a la búsqueda. | El sistema gestiona la excepción y notifica al usuario. |
