# CU52 – Modificar Escritura

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU52 – Modificar Escritura |
| **Actores** | Escribano, Cliente |
| **Propósito** | Modifica una escritura. |
| **Descripción** | El Escribano indica los datos necesarios para buscar la Escritura deseada. El Escribano modifica el detalle de la escritura, y luego guarda los cambios realizados. |
| **Tipo** | Primario y esencial |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #27 (Generar escrituras), RF #30 (Informar qué escritura(s) conforman un trámite), RF #31 (Modificar escritura); CU62, CU63 |
| **GitHub ID** | #205 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Escribano decide modificar una Escritura por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura. |  |
| 2 |  | Muestra una lista de todas las Escrituras encontradas por el valor indicado. |
| 3 | El Escribano selecciona una Escritura de la lista. |  |
| 4 |  | Muestra la escritura seleccionada donde figura: (Número de escritura; Fecha; Números de folio utilizados (de una lista de folios disponibles); Trámites involucrados; Cuerpo de escritura; Si fue firmada o no/anulada/no paso) |
| 5 | El Escribano modifica alguno de los datos de la escritura, y confirma los cambios realizados. |  |
| 6 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen escrituras con los valores indicados. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No se puede modificar el folio hasta, ya que los folios disponibles no son correlativos. | El sistema gestiona la excepción y notifica al usuario. |
