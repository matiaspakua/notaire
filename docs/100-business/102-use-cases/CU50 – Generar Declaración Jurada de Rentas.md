# CU50 – Generar Declaración Jurada de Rentas

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU50 – Generar Declaración Jurada de Rentas |
| **Actores** | Escribano/Recepcionista |
| **Propósito** | Genera la declaración jurada de Rentas, de un mes. |
| **Descripción** | El Escribano/Recepcionista decide generar las DDJJ de un mes, para lo cual el sistema solicita, que se indique el mes, año y Registro de Escribano, para generar la misma. El Escribano/Recepcionista ingresa los datos solicitados, y el sistema presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, tipo de protocolo y demás datos necesarios. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #51 (Generar declaraciones juradas), RF #53 (Generar DDJJ para Rentas), RF #54 (Imprimir declaraciones juradas); CU19, CU62 |
| **GitHub ID** | #203 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista decide generar las DDJJ de un mes. |  |
| 2 |  | Solicita, que se indique el mes, año y Registro de Escribano, para generar la misma |
| 3 | El Escribano/Recepcionista ingresa los datos solicitados. |  |
| 4 |  | Presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, y: (Número de escritura; Compraventa: [Total o terreno, Proporción parte indivisa, Remate: Judicial o Banco Hipotecario.]; Precio de la operación; Forma de pago: [Contado, Hipoteca: Saldo]; Nomenclatura Catastral; Valuación Fiscal; Impuesto; Total impuesto a pagar) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen trámites que generen DDJJ para el período indicado. | El sistema devuelve HTTP 200 y un PDF de estado vacío (no inventa 404). |
| 4.2 | No hay tabla de impuesto, clasificación de operación ni forma de pago. | Esas celdas se muestran como `N/A`. No se inventa texto legal, pie ni firma. |

## Entrada en la UI (Next.js)

Los reportes (índices, DDJJ, etc.) se alcanzan desde **Reportes** en la
navegación primaria (`/dashboard/reportes`; live `/dashboard/reports`) y
desde **Protocolo** (`/dashboard/protocol`) — issue #1058 / #203.

`GET /api/v1/reports/tax-sworn-statement` requiere `year` y `month`.
`idNotary` y `idFolioType` son filtros opcionales para el registro y el
tipo de protocolo. Titular y suplente salen de las personas y
`substitutions` registradas. Precio de `registration_drafts.operation_price`
cuando existe; valuación fiscal y nomenclatura del inmueble del trámite
en protocolo no auxiliar. Un período sin escrituras notifica al usuario
con el PDF de estado vacío.
