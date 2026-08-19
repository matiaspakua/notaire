# CU12 – Retirar testimonio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU12 – Retirar testimonio |
| **Actores** | Cliente, Recepcionista |
| **Propósito** | Registra el retiro de copias, de un testimonio, de una escritura. |
| **Descripción** | Una Persona se acerca a la escribanía, y solicita retirar las copias de testimonio, de una escritura, para lo cual busca todas las escrituras firmadas del escribano indicado o si se indica el número de escritura se muestra el detalle de la misma. El Recepcionista indica los datos necesarios para registrar el retiro de las copias. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #35 (Registrar retiro de testimonio); CU62 |
| **GitHub ID** | #165 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Una Persona se acerca a la escribanía, y solicita retirar copias de un testimonio, de una escritura. |  |
| 2 | El Recepcionista solicita el retiro de copias de un testimonio de una Escritura en particular. |  |
| 3 |  | Busca y muestra todas las escrituras firmadas. |
| 4 | El Recepcionista selecciona la escritura correspondiente. |  |
| 5 |  | Muestra los datos de los testimonios asociados a la escritura: (Registro de Escribano; Número de Escritura; Fecha de Escrituración; Folio desde y hasta utilizados) Por cada testimonio generado: (Número de Testimonio; Fecha de Impresión; Si fue inscripto o no; Cantidad de copias generadas; Observaciones) y solicita nombre, apellido, tipo y número de identificación, del Cliente que retira la copia. |
| 6 | El Recepcionista solicita al Cliente el nombre, apellido, tipo y número de identificación. |  |
| 7 | El Cliente indica los datos solicitados. |  |
| 9 | El Recepcionista indica los datos al sistema y la cantidad de copias a retirar. |  |
| 10 |  | Registra el retiro de la copia y la fecha de retiro. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 10.2 | 1. El Recepcionista informa al Cliente de la deuda registrada. 2. El Cliente decide no retirar la copia. | El sistema gestiona la excepción y notifica al usuario. |
| 10.2 | El Recepcionista informa al Cliente de la deuda registrada. (Seguir en el punto 6.) | Informa que el Cliente registra deudas sobre el presupuesto de la gestión, asociada a la copia a retirar. |
| 5.2 | No existen copias de testimonio a retirar, asociadas al Cliente indicado. | El sistema gestiona la excepción y notifica al usuario. |
