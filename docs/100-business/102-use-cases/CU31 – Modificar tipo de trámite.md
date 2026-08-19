# CU31 – Modificar tipo de trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU31 – Modificar tipo de trámite |
| **Actores** | Escribano |
| **Propósito** | Modifica un tipo de trámite. |
| **Descripción** | El Escribano decide modificar un tipo de trámite. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite y el sistema presenta los datos del mismo. El Escribano modifica alguno de los datos del tipo de trámite seleccionado. Luego guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #56 (Ingresar nuevos trámites); CU64 |
| **GitHub ID** | #184 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar un tipo de trámite. |  |
| 2 |  | Presenta una lista de todos los tipos de trámite disponibles. |
| 3 | El Escribano selecciona un tipo de trámite. |  |
| 4 |  | Presenta: (Nombre del trámite; Si requiere inscripción o no; Si se archiva o no; Si se asocia con inmuebles; Observaciones; Muestra además, una lista de todos los tipos de documento asociados.) |
| 5 | El Escribano modifica: (Si requiere inscripción o no; Si se archiva o no; Si se asocia con inmuebles; Tipos de documento asociados y/o; Observaciones) |  |
| 6 |  | Guarda los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de trámite disponibles. | El sistema gestiona la excepción y notifica al usuario. |
