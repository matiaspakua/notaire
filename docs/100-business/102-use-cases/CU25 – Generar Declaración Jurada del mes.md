# CU25 – Generar Declaración Jurada del mes

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU25 – Generar Declaración Jurada del mes |
| **Actores** | Escribano/Recepcionista |
| **Propósito** | Genera la declaración jurada de un mes. |
| **Descripción** | El Escribano/Recepcionista decide generar las DDJJ de un mes, para lo cual el sistema solicita, que se indique el mes, año y Registro de Escribano, para generar la misma. El Escribano/Recepcionista ingresa los datos solicitados, y el sistema presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, y demás datos necesarios. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #51 (Generar declaraciones juradas), RF #52 (Generar DDJJ a partir de escrituras realizadas en el mes), RF #54 (Imprimir declaraciones juradas); CU19, CU62 |
| **GitHub ID** | #178 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista decide generar las DDJJ de un mes. |  |
| 2 |  | Solicita, que se indique el mes, año y Registro de Escribano, para generar la misma |
| 3 | El Escribano/Recepcionista ingresa los datos solicitados. |  |
| 4 |  | Presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, tipo de protocolo y: (Número de escritura; Número del primer folio utilizado; Día del mes; Actos que comprenden la escritura e involucrados; Anexo, si existe; Nombre de los contratantes; Valor imponible: [Valuación fiscal especial, Precio]; Tasa; Impuesto; Total tasa; Total Impuesto) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen escrituras para el mes y año indicados. | El sistema devuelve HTTP 200 y un PDF de estado vacío (no inventa 404). |
| 4.2 | No hay tabla de tasa/impuesto ni entidad de anexo. | Las celdas de anexo, tasa e impuesto se muestran como `N/A`. No se inventa texto legal, pie ni firma. |

## Entrada en la UI (Next.js)

Los reportes (índices, DDJJ, etc.) se alcanzan desde **Reportes** en la
navegación primaria (`/dashboard/reportes`; live `/dashboard/reports`) y
desde **Protocolo** (`/dashboard/protocol`) — issue #1058 / #178.

`GET /api/v1/reports/monthly-sworn-statement` requiere `year` y `month`.
`idNotary` y `idFolioType` son filtros opcionales para el registro y el
tipo de protocolo. Titular y suplente salen de las personas y
`substitutions` registradas. Precio de `registration_drafts.operation_price`
cuando existe; valuación fiscal y nomenclatura del inmueble del trámite
en protocolo no auxiliar. Un período sin escrituras notifica al usuario
con el PDF de estado vacío.
