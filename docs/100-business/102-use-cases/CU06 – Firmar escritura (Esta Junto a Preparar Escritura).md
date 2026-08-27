# CU06 – Firmar escritura

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU06 – Firmar escritura |
| **Actores** | Escribano, Cliente |
| **Propósito** | Permite aprobar una escritura. |
| **Descripción** | El o los Clientes se acercan a la escribanía para firmar una escritura. El Escribano selecciona de la lista de escrituras disponibles para firmar, aquella correspondiente. Luego se realiza la firma de la escritura, por cada una de las partes, incluido el Escribano. Finalmente éste indica al sistema que la escritura ha sido firmada. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #27 (Generar escrituras), RF #29 (Firmar escrituras), RF #121 (Control de numeración de escrituras); CU62 |
| **GitHub ID** | #159 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un/os Clientes se acercan a la escribanía para firmar una escritura. |  |
| 2 | El Escribano solicita la lista de escrituras disponibles para firmar. |  |
| 3 |  | Busca y presenta una lista de escrituras habilitadas para firmar. |
| 4 | El Escribano selecciona la escritura necesaria. |  |
| 5 |  | Muestra la información de la escritura seleccionada: (Número de Escritura; Fecha; Estado; Folios utilizados; Matrícula de inscripción y fecha (si corresponde); Observaciones; Clientes Involucrados) |
| 6 | El Escribano indica que la escritura ha sido firmada. |  |
| 7 |  | Guarda los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La escritura no se encuentra dentro de la lista de escrituras disponibles para firmar. | El sistema gestiona la excepción y notifica al usuario. |
| 6.1 | La escritura ya se encuentra firmada. | El sistema gestiona la excepción y notifica al usuario. |
| 6.2 | La escritura no tiene folio(s) asignado(s). | El sistema gestiona la excepción y notifica al usuario. |
