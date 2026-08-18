# CU60 – Buscar Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU60 – Buscar Presupuesto |
| **Actores** | Recepcionista/Gestor |
| **Propósito** | Busca un presupuesto. |
| **Descripción** | El Recepcionista/Gestor requiere buscar un presupuesto en particular, para lo cual informa al sistema alguno de los siguientes datos: el número de presupuesto o el nombre y apellido del cliente o el tipo y número de identificación. El sistema busca y retorna el o los presupuesto encontrados y muestra un reporte con la información del mismo. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #4 (Preparar Presupuestos), RF #5 (Procesar solicitud de presupuestos), RF #7 (Imprimir presupuestos); CU61 |
| **GitHub ID** | #213 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Recepcionista/Gestor requiere un presupuesto. |  |
| 2 |  | Solicita que se indique alguno de los siguientes datos: (el número de presupuesto; el nombre y apellido del cliente; el tipo y número de identificación) |
| 3 | Indica alguno de los datos solicitados. |  |
| 4 |  | Busca y retorna el / los presupuesto correspondientes. |
| 5 | Selecciona un presupuesto. |  |
| 6 |  | Presenta un reporte del presupuesto indicado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existe el / los presupuesto indicados. | El sistema gestiona la excepción y notifica al usuario. |
