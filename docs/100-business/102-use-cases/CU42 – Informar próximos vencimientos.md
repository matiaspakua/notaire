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
