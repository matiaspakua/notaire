# CU08 – Verificar Testimonio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU08 – Verificar Testimonio |
| **Actores** | Escribano |
| **Propósito** | Permite verificar un testimonio generado, registrando si quedó observado y, de ser así, el motivo. |
| **Descripción** | El Escribano selecciona una escritura, de una lista de escrituras. El sistema presenta la información sobre los testimonios expedidos para la escritura seleccionada, indicando: número de testimonio, fecha de expedición, cantidad de copias generadas y observaciones. El Escribano selecciona el testimonio a verificar e indica si quedó observado; en caso afirmativo, indica el motivo de la observación. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #33 (Generar y registrar testimonios de escrituras), RF #120 (Impresión de testimonios); CU62 |
| **GitHub ID** | #161 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide verificar la cantidad de testimonios expedidos para una escritura, por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura. |  |
| 2 |  | Presenta una lista de escrituras, donde figura: (Escribano; Número de escritura; Fecha; Estado; Matrícula y fecha de inscripción (si corresponde)) de las Escrituras encontradas. |
| 3 | El Escribano selecciona una escritura de la lista. |  |
| 4 |  | El sistema presenta la información sobre los testimonios expedidos para la escritura seleccionada, indicando: (Número de testimonio; Fecha de expedición; Si está o no inscripto; Cantidad de Copias generadas; Observaciones) de cada Testimonio generado para esa Escritura. |
| 5 | El Escribano selecciona el testimonio a verificar e indica si quedó observado o no observado, junto con el motivo de la observación (si corresponde). |  |
| 6 |  | Registra la verificación del testimonio (observado/no observado y motivo). |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La escritura seleccionada no tiene testimonios expedidos. | El sistema gestiona la excepción y notifica al usuario. |
