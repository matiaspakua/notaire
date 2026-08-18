# CU80 – Administrar Cuadernos de Folios

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU80 – Administrar Cuadernos de Folios |
| **Actores** | Escribano, Gestor |
| **Propósito** | Permite organizar y agrupar los folios notariales de a diez en cuadernos consecutivos, numerarlos correlativamente de 1 a N y generar la carátula oficial con los datos del registro y detalle de los trámites que contiene. |
| **Descripción** | Para dar cumplimiento a la normativa notarial, los folios asignados al protocolo se agrupan en cuadernos de 10 folios de forma estrictamente correlativa. El sistema permite generar estos cuadernos, asignarles número secuencial (1..N por año/registro) y emitir su carátula reglamentaria indicando año, registro notarial, número de cuaderno y el detalle de escrituras/trámites otorgados en dichos folios. |
| **Tipo** | Primario / Protocolos |
| **Referencias Cruzadas** | RF #100 (Administrar cuadernos), RF #101 (Generar cuadernos), RF #102 (Numerar cuadernos), RF #103 (Generar carátula de cuaderno); CU28, CU33, CU63 |
| **GitHub ID** | #311 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano solicita generar nuevos cuadernos de folios para un protocolo y año determinados. | Presenta los folios disponibles cargados en el sistema ordenados por numeración correlativa. |
| 2 | El Escribano indica la cantidad de folios o el rango de folios consecutivos a agrupar. | Verifica que la cantidad de folios sea múltiplo exacto de 10 y que la numeración sea continua sin faltantes. |
| 3 | El Escribano confirma la generación de los cuadernos. | Agrupa los folios de a 10, asigna numeración correlativa consecutiva (1 a N) a cada cuaderno y actualiza el estado de los folios a "Asignados a cuaderno". |
| 4 | El Escribano solicita la emisión de la carátula de un cuaderno específico. | Genera el formato de carátula oficial conteniendo: año, número de registro del escribano, número de cuaderno, rango de folios y detalle de las escrituras/trámites otorgados. |
| 5 | El Escribano procede a la impresión de la carátula. | Habilita la impresión en papel oficial o exportación a PDF de la carátula. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | Rango de folios con faltantes o numeración discontinua | El sistema alerta que los folios deben ser estrictamente consecutivos e impide generar el cuaderno hasta resolver la correlatividad. |
| 2.2 | Existen folios en estado dañado (errose) o anulado (no pasó) en el lote | El sistema identifica los folios afectados, solicita justificación e incluye la observación correspondiente en la composición del cuaderno. |
| 3.1 | Conflicto con numeración de cuaderno ya existente en el año | El sistema recalcula el siguiente número correlativo disponible para el registro notarial. |
