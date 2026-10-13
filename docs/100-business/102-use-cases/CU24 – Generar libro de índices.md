# CU24 – Generar libro de índice

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU24 – Generar libro de índice |
| **Actores** | Escribano/Recepcionista |
| **Propósito** | Permite generar el libro de índice de un año, registro y protocolo determinado. |
| **Descripción** | El Escribano/Recepcionista requiere la generación de un libro de índice. El sistema solicita que se ingrese el año, registro de Escribano y tipo de protocolo. El sistema presenta el índice generado donde figura: ordenado alfabéticamente por nombre de cliente, las personas involucradas en cada gestión registrada, incluyendo nombre y apellido de cada uno, nombre del trámite, número de escritura, número del primer folio utilizado, día y mes en que se firmó la misma. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #48 (Generar índices), RF #49 (Ver índices de trámites), RF #50 (Permitir editar e imprimir los índices); CU19, CU62 |
| **GitHub ID** | #177 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista requiere la generación de un libro de índice. |  |
| 2 |  | Solicita que se ingrese el año, registro de Escribano. |
| 3 | El Escribano/Recepcionista ingresa los datos solicitados. |  |
| 4 |  | Presenta el índice generado donde figura: (Ordenado alfabéticamente las personas involucradas en cada gestión registrada, incluyendo nombre y apellido de cada uno; Nombre del trámite; Número de escritura; Número del primer folio utilizado; Día y mes en que se firmó la misma) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen escrituras para el año y registro indicados. | El sistema devuelve HTTP 200 y un PDF de estado vacío (no inventa 404). |
| 4.2 | Reporte con ID inexistente | No aplica: el libro de índice se filtra por período, no por un ID de reporte. |

## Entrada en la UI (Next.js)

Los reportes (índices, DDJJ, etc.) se alcanzan desde **Reportes** en la
barra lateral / inicio (`/dashboard/reports`) y desde **Protocolo**
(`/dashboard/protocol`) — issue #1058 / #177.

`GET /api/v1/reports/index-book` requiere `year`. `idNotary` y `idFolioType`
son filtros opcionales para el registro y el tipo de protocolo. Un período
sin escrituras notifica al usuario con el PDF de estado vacío. No se inventa
texto legal de pie o firma.
