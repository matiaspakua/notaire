# CU10 – Registrar movimientos de documentación de entidades externas

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU10 – Registrar movimientos de documentación de entidades externas |
| **Actores** | Gestor/Escribano |
| **Propósito** | Permite registrar la documentación entregada, para una gestión determinada. |
| **Descripción** | El Gestor/Escribano selecciona una gestión, de una lista de gestiones en trámite. El sistema muestra número de gestión, personas involucradas, trámites asociados, nomenclatura catastral en caso de tratarse de un inmueble, y documentación asociada a la misma, que debe ser presentada por entidades externas. El Gestor/Escribano registra para un documento los datos necesarios. Luego guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #18 (Informar preparación de documentos), RF #19 (Informar seguimiento de documentos); CU19 |
| **GitHub ID** | #163 |
| **Implementación** | Pantalla `/dashboard/documentos-entidades-externas` (Issue #863) — lista de gestiones, detalle de documentación a cargo de entidades externas y diálogo para registrar movimiento (número de cartón, fechas, importe, observado, observaciones, entregado). Backend: `DocumentoEntidadExternaService`, `GET/PUT /api/v1/gestiones/{id}/documentos-entidades-externas[/{idDocumentoPresentado}]`; al completarse toda la documentación la gestión pasa a estado "documentación completa" automáticamente. |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano solicita la lista de gestiones de un cliente determinado. |  |
| 2 |  | Busca y presenta la lista de gestiones indicadas, indicando: (Número de gestión; Encabezado; Fecha de inicio; Estado; Número de carpeta; Número de bibliorato; Observaciones) |
| 3 | El Gestor/Escribano selecciona una gestión. |  |
| 4 |  | Muestra la gestión indicada, detallando: (Número de gestión,; Encabezado,; Fecha de inicio,; Escribano a cargo,; Nomenclatura Catastral si corresponde,; Documentación asociada a la misma, que debe ser presentada por entidades externas, indicando: [Nombre documento,, Si fue preparado o no,, Número de cartón,, Fecha de ingreso,, Fecha de salida,, Si fue observado,, Monto deuda,, Fecha de pago,, Fecha de liberación,, Observaciones,, Finalizado o no]) |
| 5 | El Gestor/Escribano los datos correspondientes y confirma los cambios realizados. |  |
| 7 |  | Guarda los cambios realizados y una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a “documentación completa”. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | Para la gestión indicada ya fueron entregados todos los documentos. | El sistema gestiona la excepción y notifica al usuario. |
| 7.1 | Alguno de los datos ingresados no es válido. | El sistema gestiona la excepción y notifica al usuario. |
