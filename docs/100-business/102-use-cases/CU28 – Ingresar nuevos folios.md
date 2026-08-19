# CU28 – Ingresar nuevos folios

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU28 – Ingresar nuevos folios |
| **Actores** | Escribano |
| **Propósito** | Registra un conjunto de folios de un Escribano. |
| **Descripción** | Un Escribano decide registrar el ingreso de nuevos folios. El sistema solicita: el número del primer y último folio del conjunto, número de registro de escribano y año. El escribano ingresa los datos indicados y guarda los cambios. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #58 (Ingresar nuevos folios), RF #94 (Administrar folios), RF #95 (Cargar folios del Colegio Notarial), RF #96 (Control de numeración correlativa de folios) |
| **GitHub ID** | #181 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Escribano decide registrar el ingreso de nuevos folios. |  |
| 2 |  | Solicita: el número del primer y último folio del conjunto, número de registro de escribano y año. |
| 3 | El escribano ingresa los datos indicados y guarda los cambios. |  |
| 4 |  | Se registra el nuevo conjunto de folios. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
