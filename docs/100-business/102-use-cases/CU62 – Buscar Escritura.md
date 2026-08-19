# CU62 – Buscar Escritura

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU62 – Buscar Escritura |
| **Actores** | Gestor/Recepcionista/Escribano, Cliente |
| **Propósito** | Busca una escritura en particular. |
| **Descripción** | Un cliente se acerca a la escribanía para consultar algún dato de una escritura que realizó. El Gestor/Recepcionista/Escribano solicita que se indique qué escribano realizó la misma o el número de escritura. El sistema busca y muestra una lista de todas las escrituras realizadas por el escribano indicado y muestra el detalle de una de las escrituras seleccionadas (si se indico el número de escritura, simplemente se muestra el detalle de esa escritura), donde se indica: el número de escritura, la fecha de escrituración, un detalle del cuerpo, el estado y las observaciones. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #27 (Generar escrituras), RF #28 (Preparar escrituras), RF #30 (Informar qué escritura(s) conforman un trámite) |
| **GitHub ID** | #215 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un cliente se acerca a la escribanía para consultar algún dato de una escritura que realizó. |  |
| 2 | Gestor/Recepcionista/Escribano solicita que se indique qué escribano realizó la misma o el número de escritura. |  |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 3.1 | El escribano indicado no posee escrituras registradas. | El sistema gestiona la excepción y notifica al usuario. |
| 3.2 | El número de escritura indicado no es válido o no existe. | El sistema gestiona la excepción y notifica al usuario. |
