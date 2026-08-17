# CU33 – Modificar folio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU33 – Modificar folio |
| **Actores** | Escribano |
| **Propósito** | Modifica las características de un folio |
| **Descripción** | El Escribano decide modificar los datos de un determinado folio. El sistema solicita que se ingrese el año y registro de escribano, y presenta una lista de todos los folios disponibles. El Escribano selecciona un folio, y el sistema presenta los datos del mismo. El escribano modifica el estado del mismo y guarda los cambios. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #58 (Ingresar nuevos folios), RF #94 (Administrar folios), RF #96 (Control de numeración correlativa de folios), RF #97 (Manejo de folios dañados (errose)), RF #98 (Manejo de folios no usados (no pasó)); CU63 |
| **GitHub ID** | #186 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar los datos de un determinado folio. |  |
| 2 |  | Solicita que se ingrese el año, indicando el año actual por default, y el número de registro de escribano correspondiente. |
| 3 | El Escribano indica el año y número de registro. |  |
| 4 |  | Busca y presenta una lista de todos los folios del año y escribano indicados. |
| 5 | El Escribano selecciona un folio. |  |
| 6 |  | Presenta: (Número de folio; Tipo de folio; Estado actual del mismo; Fecha del último estado; Observaciones) |
| 7 | El escribano modifica el estado del mismo confirmando los cambios. |  |
| 8 |  | Guarda los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen folios cargados para el año indicado. | El sistema gestiona la excepción y notifica al usuario. |
