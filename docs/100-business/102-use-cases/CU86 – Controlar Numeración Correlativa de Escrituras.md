# CU86 – Controlar Numeración Correlativa de Escrituras

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU86 – Controlar Numeración Correlativa de Escrituras |
| **Actores** | Escribano |
| **Propósito** | Garantizar que el número de escritura asignado al preparar o firmar una escritura sea único y correlativo dentro del protocolo del escribano, sin faltantes ni duplicados. |
| **Descripción** | Al preparar una escritura (CU05) o registrarla como firmada (CU06), el Escribano ingresa hoy el número de escritura como texto libre, sin ninguna validación. Este caso de uso agrega el control que falta: antes de guardar, el sistema verifica que el número propuesto sea el siguiente correlativo disponible para el protocolo y año del escribano, y rechaza o alerta ante números repetidos o con saltos no justificados (por ejemplo, escritura anulada o "no pasó"). |
| **Tipo** | Primario / Protocolos |
| **Referencias Cruzadas** | RF #121 (Control de numeración de escrituras); CU05, CU06 |
| **GitHub ID** | _pendiente — se completa al crear el issue de implementación_ |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano completa el detalle de una escritura, incluyendo el número de escritura (CU05, paso 5). | |
| 2 | | Calcula el siguiente número correlativo esperado para el protocolo y año del escribano, en base a las escrituras ya registradas. |
| 3 | | Compara el número ingresado por el Escribano contra el correlativo esperado. |
| 4 | El Escribano confirma el guardado. | Si el número coincide con el correlativo esperado, guarda la escritura con normalidad. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 3.1 | El número ingresado ya fue usado por otra escritura del mismo protocolo/año | El sistema rechaza el guardado y notifica el número duplicado. |
| 3.2 | El número ingresado deja un salto respecto al correlativo esperado | El sistema alerta el salto y solicita justificación (por ejemplo, escritura anulada o "no pasó"), registrando la observación junto al número saltado. |
| 3.3 | La escritura se registra en protocolo auxiliar (CU81) | El sistema aplica la correlatividad dentro de la numeración propia del protocolo auxiliar, independiente de la del protocolo principal. |
