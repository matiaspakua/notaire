# CU16 – Archivar Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU16 – Archivar Gestión |
| **Actores** | Escribano/Gestor/Protocolista |
| **Propósito** | Finaliza una gestión. |
| **Descripción** | El Escribano/Gestor/Protocolista selecciona de una lista de gestiones en trámite disponibles, una de ellas y cambia el estado de la gestión a “archivada” e indica observaciones. El sistema valida que la transición al estado "Archivada" sea alcanzable desde el estado actual según el workflow definido para el tipo de trámite (CU83); si no lo es, rechaza el archivado con un mensaje visible. El sistema calcula el saldo pendiente agregado de la gestión y, si es mayor a cero, advierte al usuario antes de confirmar (la advertencia no bloquea el archivado). Al archivar la gestión, el sistema cambia también el estado de todas sus carpetas de trámite (CU85) a "Archivada"; si alguna carpeta sigue en "Espera" sin resolver, alerta y exige confirmación explícita antes de archivar. El sistema genera un nuevo número de archivo indicando número de bibliorato y número de carpeta, para la misma, registra si la gestión quedó archivada con deuda pendiente y agrega la entrada correspondiente a la bitácora (CU13). |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #22 (Verificar deuda pendiente al archivar), RF #36 (Archivar trámites), RF #37 (Archivar trámite), RF #104 (Administrar carpetas de trámite), RF #106 (Estados de carpeta); CU13, CU19, CU83, CU85 |
| **GitHub ID** | #169, #819, #833, #839 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Gestor/Protocolista solicita al sistema una lista de gestiones en trámite. |  |
| 2 |  | Busca y presenta la lista de gestiones indicada. |
| 3 | El Escribano/Gestor/Protocolista selecciona una gestión de la lista para archivar. |  |
| 3.1 |  | Calcula el saldo pendiente agregado de la gestión (suma del saldo pendiente de cada presupuesto vinculado a sus trámites). Si el saldo es mayor a cero, presenta una advertencia de deuda pendiente antes de pedir confirmación. |
| 4 | El Escribano/Gestor/Protocolista confirma el archivado (con o sin deuda pendiente) e indica algunas observaciones. |  |
| 5 |  | Registra los cambios realizados, genera un nuevo número de archivo indicando número de bibliorato y número de carpeta de archivo, y persiste si la gestión quedó archivada con deuda pendiente. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La gestión ya tiene un número de archivo asociado. | El sistema gestiona la excepción y notifica al usuario. |
| 3.1.1 | El saldo pendiente agregado de la gestión es igual a cero. | El sistema archiva la gestión directamente, sin mostrar advertencia. |
| 3.2 | El workflow definido para el tipo de trámite de la gestión no admite una transición desde el estado actual hacia "Archivada". | El sistema rechaza el archivado e informa que la transición no está permitida. |
| 3.3 | Alguna carpeta de trámite (CU85) de la gestión sigue en estado "Espera" sin resolver. | El sistema alerta al usuario y exige confirmación explícita antes de archivar de todos modos. |
