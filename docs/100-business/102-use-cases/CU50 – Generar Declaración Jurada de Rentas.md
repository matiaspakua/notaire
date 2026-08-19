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
| 4.1 | No existen trámites que generen DDJJ para el período indicado. | El sistema gestiona la excepción y notifica al usuario. |
