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
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
