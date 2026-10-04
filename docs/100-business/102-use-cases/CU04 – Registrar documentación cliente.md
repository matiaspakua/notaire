# CU04 – Registrar documentación cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU04 – Registrar documentación cliente |
| **Actores** | Gestor/Recepcionista, Cliente |
| **Propósito** | Registra y controla documentos entregados por un Cliente. |
| **Descripción** | El Gestor/Recepcionista busca la gestión asociada a un cliente en particular. El sistema presenta toda la documentación necesaria para dicha gestión. El Gestor/Recepcionista verifica e indica los documentos entregados por el Cliente, y guarda los cambios realizados. Una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a documentación completa. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #18 (Informar preparación de documentos); CU19 |
| **GitHub ID** | #157 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Recepcionista busca la gestión asociada a un cliente en particular, indicando nombre y apellido, o tipo y número de identificación del cliente. |  |
| 2 |  | El sistema presenta: (Número de gestión,; Encabezado,; Fecha de inicio,; Escribano a cargo,; Documentos asociados: [Tipo de documento, Si fue entregado o no]) |
| 3 | El Gestor/Recepcionista verifica e indica los documentos entregados por el Cliente y los registra. |  |
| 4 |  | Guarda los cambios realizados y una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a “documentación completa”. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existe la gestión | El sistema gestiona la excepción y notifica al usuario. |

## Implementación (#773)

La pantalla `/dashboard/documentos` registra un documento presentado eligiendo el tipo, la fecha, si fue entregado y, opcionalmente, una gestión y uno de sus trámites (`POST /api/v1/documento-presentado` con `typeId`, `date`, `delivered` y `procedureId`). El listado muestra el tipo, la fecha y el trámite vinculado. El resumen del caso de la gestión (`GET /api/v1/gestiones/{id}/resumen-caso`, diálogo *Resumen del caso* de la pantalla de gestiones) lista los documentos de sus trámites con su estado: preparado, liberado, observado, entregado, reingresado y fecha de vencimiento.
