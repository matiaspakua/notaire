# CU42 – Informar próximos vencimientos

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU42 – Informar próximos vencimientos |
| **Actores** | Gestor, Escribano |
| **Propósito** | Informa los próximos vencimientos de los documentos. |
| **Descripción** | Se verifica en todos los documentos presentados, si poseen cuales están próximos a vencer y se le informa al Gestor/Escribano el día de vencimiento de cada uno de los documentos, junto a los siguientes datos de cada uno: Nombre de documento, Número de gestión, Encabezado, Si fue preparado, Fecha de Ingreso, Fecha de Salida, Número de cartón, Si fue observado, Monto de deuda($), Fecha de pago, Fecha de liberación Observaciones |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #19 (Informar seguimiento de documentos) |
| **GitHub ID** | #195 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 |  | Busca en todos los documentos presentados que estén próximos a vencer, y se informan los siguientes datos de cada uno: (Nombre de documento,; Número de gestión,; Encabezado,; Si fue preparado,; Fecha de Ingreso,; Fecha de Salida,; Número de cartón,; Si fue observado,; Monto de deuda($),; Fecha de pago,; Fecha de liberación; Observaciones) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | No existen documentos próximos a vencer. | El sistema gestiona la excepción y notifica al usuario. |

## Herencia de vencimiento desde el tipo de documento (Issue #837)

El vencimiento (`vence`, `diasVencimiento`, `quienEntrega`) se configura una
única vez en el tipo de documento (CU27/CU32) y se hereda automáticamente a
cada `DocumentoPresentado` creado a partir de ese tipo, calculando
`fechaVencimiento = fechaIngreso + diasVencimiento`. Antes de este cambio
ningún tipo de documento tenía estos campos cargables desde la pantalla de
administración, por lo que este informe nunca tenía datos reales sobre los
que operar.

## Implementación (#802)

`GET /api/v1/documento-presentado/proximos-vencimientos?dias=N` (`UpcomingExpirationController`, `UpcomingExpirationService`) devuelve los documentos presentados que vencen, no están liberados y cuya fecha de vencimiento está entre hoy y hoy más `N` días (ambos inclusive), ordenados por fecha de vencimiento. `N` va de 1 a 365 (por defecto 30); fuera de rango responde 400. Cada fila trae los datos de este caso de uso (nombre del documento, número y encabezado de la gestión, preparado, fecha de ingreso y de salida, número de cartón, observado, monto de deuda, fecha de pago, fecha de liberación, observaciones) más la fecha de vencimiento y los días restantes. Los documentos ya vencidos o liberados no se informan. Pantalla: `/dashboard/proximos-vencimientos`, con selector de ventana y mensaje cuando no hay documentos (excepción 1.1).
