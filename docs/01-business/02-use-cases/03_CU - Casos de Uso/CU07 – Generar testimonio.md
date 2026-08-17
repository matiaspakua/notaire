# CU07 – Generar testimonio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU07 – Generar testimonio |
| **Actores** | Escribano |
| **Propósito** | Permite generar un testimonio de una escritura. |
| **Descripción** | El Escribano selecciona una escritura, de una lista de escrituras firmadas. El sistema muestra los datos principales de la escritura seleccionada. El Escribano indica datos necesarios para generar un testimonio. Una vez finalizado esto, se registra el nuevo testimonio y sus copias. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #32 (Administrar inscripciones), RF #33 (Generar y registrar testimonios de escrituras), RF #120 (Impresión de testimonios); CU62 |
| **GitHub ID** | #160 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano desea generar el Testimonio de una Escritura por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura. |  |
| 2 |  | Presenta una lista de escrituras, donde figura: (Escribano; Número de escritura; Fecha; Estado; Matrícula y fecha de inscripción (si corresponde)) de las Escrituras encontradas. |
| 3 | El Escribano selecciona una escritura. |  |
| 4 |  | El sistema muestra los datos principales de la escritura seleccionada: (Número de escritura; Fecha; Folios utilizados) |
| 5 | El Escribano indica: (Fecha de generación; Cantidad de copias a generar del Testimonio; Observaciones) Confirma la generación del Testimonio. |  |
| 6 |  | Registra el nuevo Testimonio. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen Escrituras Firmadas. | El sistema gestiona la excepción y notifica al usuario. |
