# CU13 – Ver historial de gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU13 – Ver historial de gestión |
| **Actores** | Gestor/Escribano |
| **Propósito** | Permite visualizar los antecedentes de una gestión. |
| **Descripción** | El Gestor/Escribano requiere información sobre una determinada gestión, por lo tanto busca en su lista de gestiones, aquella que le interesa, por nombre de cliente o tipo y número de documento. El Gestor/Escribano selecciona la gestión deseada del resultado de la búsqueda. El sistema presenta el historial de la gestión, donde se listan todos los estados por los cuales ha pasado la gestión, indicando para cada estado: número de gestión, encabezado, fecha de inicio, estado, número de archivo, número de bibliorato y observaciones. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #24 (Consultar estado e historial de los trámites), RF #26 (Informar el historial de un trámite), RF #110 (Registrar historial de cambios de estado); CU19, CU83. Historial expuesto vía `GET /api/v1/gestiones/{id}/historial` y consultable desde la pantalla de gestiones ("Ver bitácora"). |
| **GitHub ID** | #166, #833 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Escribano requiere información sobre una determinada gestión, por lo tanto, solicita al sistema una lista de gestiones, indicando el nombre y apellido, o tipo y número de identificación de un cliente. |  |
| 2 |  | Busca y presenta una lista de gestiones, según los parámetros indicados. |
| 3 | El Gestor/Escribano selecciona una gestión en particular. |  |
| 4 |  | El sistema presenta el historial de la gestión, donde figuran: (Número de Gestión; Encabezado; Fecha de inicio; Escribano a cargo; Estados por lo cuales ha pasado con sus fechas y observaciones asociados en cada paso.; Número de archivo,; Número de bibliorato.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | Dentro del resultado de la búsqueda, no se encuentra la gestión deseada. | El sistema gestiona la excepción y notifica al usuario. |
