# CU05 – Preparar escritura

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU05 – Preparar escritura |
| **Actores** | Escribano, Cliente |
| **Propósito** | Genera una nueva escritura. |
| **Descripción** | El Escribano selecciona una gestión, de la lista de gestiones habilitadas para escriturar, para preparar una escritura. El sistema muestra la gestión seleccionada donde figuran las personas involucradas (Clientes y Escribano titular/suplente), el/los trámite/s de la gestión, los folios utilizados y la fecha de inicio de la misma. El Escribano completa el detalle de la escritura, luego el Escribano guarda los datos indicados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #27 (Generar escrituras), RF #28 (Preparar escrituras), RF #30 (Informar qué escritura(s) conforman un trámite), RF #119 (Diferencias entre protocolo principal y auxiliar), RF #121 (Control de numeración de escrituras); CU19, CU63 |
| **GitHub ID** | #158 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Escribano decide generar una nueva Escritura. |  |
| 2 |  | Muestra una lista de todas las Gestiones habilitadas para Escriturar. |
| 3 | El Escribano selecciona una Gestión para escriturar de la lista. |  |
| 4 |  | Muestra la gestión seleccionada donde figura: (Número de gestión; Encabezado; Fecha de inicio; Escribano a cargo; Nomenclatura catastral (si corresponde); Trámites asociados; Clientes involucrados; Lista de folios disponibles.) |
| 5 | El Escribano completa el detalle de la escritura, Registrando: (Número de escritura; Fecha; Números de folio utilizados (de una lista de folios disponibles); Trámites involucrados; Cuerpo de escritura; Si fue firmada o no/anulada/no paso (*)) |  |
| 6 |  | Registra la operación realizada por el usuario y cambia el estado de la Gestión. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen gestiones habilitadas para escriturar. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No hay folios disponibles. | El sistema gestiona la excepción y notifica al usuario. |
