# CU39 – Crear Plantilla Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU39 – Crear Plantilla Presupuesto |
| **Actores** | Escribano |
| **Propósito** | Crea la plantilla de un presupuesto |
| **Descripción** | El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite, y el sistema muestra la lista de conceptos disponibles. El Escribano indica los conceptos que se deben abonar para el mismo. Luego guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #6 (Editar plantillas de presupuestos), RF #61 (Administrar plantillas), RF #66 (Plantillas de presupuestos), RF #67 (Crear nuevas plantillas de presupuestos); CU64, CU66 |
| **GitHub ID** | #192 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular. |  |
| 2 |  | Presenta una lista de todos los tipos de trámite disponibles. |
| 3 | El Escribano selecciona un tipo de trámite de la lista. |  |
| 4 |  | Muestra la lista de conceptos disponibles para ese trámite. |
| 5 | El Escribano selecciona los conceptos asociados al mismo, y luego confirma la nueva plantilla de presupuesto. |  |
| 6 |  | Registra una nueva plantilla de presupuesto. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | 1. No existen tipos de trámite disponibles. | El sistema gestiona la excepción y notifica al usuario. |
| 2.1 | 2. Los tipos de trámite existentes ya tiene una plantilla de presupuesto asociada. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | No existen conceptos registrados. | El sistema gestiona la excepción y notifica al usuario. |

## Gastos por tipo de documento (Issue #823)

Además de los conceptos, la plantilla de presupuesto de un tipo de trámite
puede definir un gasto fijo o variable (porcentaje) esperado por cada tipo
de documento asociado a ese trámite (`PlantillaCostoDocumento`). Debe
indicarse exactamente uno de los dos (monto fijo o porcentaje variable), no
ambos ni ninguno. Ver CU27 – Ingresar nuevo tipo de documento para cómo ese
costo se refleja luego en el presupuesto de un trámite concreto.

## Cargar ítems de la plantilla en un presupuesto real (Issue #834)

Desde la pantalla de presupuestos (`/dashboard/presupuestos`), el
Escribano puede abrir el diálogo de ítems de un presupuesto ya creado,
elegir un `TipoDeTramite` y cargar de un solo paso, como ítems del
presupuesto, los conceptos definidos en la `PlantillaPresupuesto` de ese
tipo de trámite (endpoint `POST
/api/v1/presupuestos/{id}/items-desde-plantilla?tipoTramiteId=X`). Si el
tipo de trámite seleccionado no tiene plantilla configurada, el sistema
informa el error y no agrega ningún ítem. Ver también CU71 – Gestión de
Items para la carga de ítems individuales desde el catálogo.
