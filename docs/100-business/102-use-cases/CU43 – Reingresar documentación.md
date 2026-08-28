# CU43 – Reingresar documentación

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU43 – Reingresar documentación |
| **Actores** | Gestor/Escribano |
| **Propósito** | Permite registrar el reingreso de un documento. |
| **Descripción** | Un documento ha sido devuelto observado o ha vencido. El Gestor/Escribano solicita el reingreso del mismo, seleccionando una gestión de una lista de gestiones disponibles. El sistema muestra los trámites asociados a la gestión seleccionada. El Gestor/Escribano selecciona un trámite, y el sistema muestra la documentación necesaria para el mismo. El Gestor/Escribano indica el tipo de documento a ser reingresado. El sistema agrega el tipo de documento seleccionado, a la lista de documentos presentados de la gestión. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #19 (Informar seguimiento de documentos); CU19 |
| **GitHub ID** | #196 |
| **Implementación** | Pantalla `/dashboard/reingreso-documentacion` (Issue #865) — lista de gestiones, diálogo de detalle con sus trámites y la documentación necesaria de cada uno (según su `PlantillaTramite`), y acción de reingreso por tipo de documento. Backend: `ReingresoDocumentacionService`, `GET/POST /api/v1/gestiones/{id}/reingreso-documentacion`; el reingreso crea un nuevo `DocumentoPresentado` con `reingresado=true`. |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano solicita la lista de gestiones de un cliente determinado. |  |
| 2 |  | Busca y presenta la lista de gestiones indicadas, indicando: (Número de gestión; Encabezado; Fecha de inicio; Estado; Número de carpeta; Número de bibliorato; Observaciones) |
| 3 | El Gestor/Escribano selecciona una gestión. |  |
| 4 |  | Muestra la gestión indicada, detallando: (Trámites asociados; Documentos por cada trámite) |
| 5 | El Gestor/Escribano selecciona un trámite. |  |
| 6 |  | Muestra la documentación necesaria para el trámite indicado. |
| 7 | El Gestor/Escribano indica el tipo de documento a ser reingresado. |  |
| 8 |  | Se agrega el tipo de documento seleccionado, a la lista de documentos presentados de la gestión. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
